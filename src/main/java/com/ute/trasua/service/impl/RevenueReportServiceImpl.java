package com.ute.trasua.service.impl;

import com.ute.trasua.dto.RevenueReportRequest;
import com.ute.trasua.dto.RevenueReportResponse;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.Order;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.repository.ImportReceiptRepository;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.service.ExcelExportService;
import com.ute.trasua.service.RevenueReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation tính toán thống kê doanh thu theo công thức chuẩn:
 * Thực thu = Tiền món + Phí vận chuyển - Voucher (Module 15 / UC30).
 */
@Service
@Slf4j
public class RevenueReportServiceImpl implements RevenueReportService {

    private final OrderRepository orderRepository;
    private final BranchRepository branchRepository;
    private final ImportReceiptRepository importReceiptRepository;
    private final ExcelExportService excelExportService;

    public RevenueReportServiceImpl(OrderRepository orderRepository,
                                    BranchRepository branchRepository,
                                    ImportReceiptRepository importReceiptRepository,
                                    ExcelExportService excelExportService) {
        this.orderRepository = orderRepository;
        this.branchRepository = branchRepository;
        this.importReceiptRepository = importReceiptRepository;
        this.excelExportService = excelExportService;
    }

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,### đ");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#,##0.0'%'");

    @Override
    @Transactional(readOnly = true)
    public RevenueReportResponse generateRevenueReport(RevenueReportRequest request) {
        if (request == null) {
            request = new RevenueReportRequest();
        }

        // 1. Xác định khoảng thời gian báo cáo
        LocalDate[] dateRange = resolveDateRange(request);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay(); // Khoảng nửa mở [start, end) để không bỏ sót các đơn lúc 23:59:59.xxx

        // 2. Xác định chi nhánh
        Long branchId = request.getBranchId();
        Branch targetBranch = null;
        String branchName = "Toàn Hệ Thống UTETra";
        String branchCode = "ALL";

        if (branchId != null) {
            targetBranch = branchRepository.findById(branchId).orElse(null);
            if (targetBranch != null) {
                branchName = targetBranch.getName();
                branchCode = targetBranch.getBranchCode();
            }
        }

        // 3.1. Truy vấn các đơn hàng trong khoảng thời gian
        List<Order> orders = orderRepository.findOrdersForReport(branchId, startDateTime, endDateTime);

        // 3.2. Truy vấn các phiếu nhập kho nguyên liệu (Chi tiêu) trong khoảng thời gian
        List<com.ute.trasua.entity.ImportReceipt> importReceipts = importReceiptRepository.searchReceipts(branchId, null, "IMPORTED", startDateTime, endDateTime);

        // 4. Tính toán các chỉ số tổng quan (KPI)
        long totalOrders = orders.size();
        long completedOrders = 0;
        long cancelledOrders = 0;

        BigDecimal totalGrossRevenue = BigDecimal.ZERO;  // Tiền món
        BigDecimal totalDiscountAmount = BigDecimal.ZERO; // Tiền giảm giá Voucher
        BigDecimal totalNetRevenue = BigDecimal.ZERO;     // Thực thu = Tiền món - Voucher

        List<Order> paidOrders = new ArrayList<>();

        for (Order order : orders) {
            // Đơn hàng đã hủy (CANCELLED) không được tính vào doanh thu thực tế và đơn hoàn thành
            if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
                cancelledOrders++;
                continue;
            }

            boolean isPaid = "PAID".equalsIgnoreCase(order.getPaymentStatus()) ||
                    "PAID".equalsIgnoreCase(order.getStatus()) ||
                    "COMPLETED".equalsIgnoreCase(order.getStatus()) ||
                    "DELIVERED".equalsIgnoreCase(order.getStatus());

            if (isPaid) {
                completedOrders++;
                paidOrders.add(order);

                BigDecimal itemTotal = (order.getTotalAmount() != null) ? order.getTotalAmount() : BigDecimal.ZERO;
                BigDecimal discount = (order.getDiscountAmount() != null) ? order.getDiscountAmount() : BigDecimal.ZERO;
                // Thực thu thuần từ bán phẩm trà = Tiền món (Gross) - Giảm giá Voucher (không hạch toán cước vận chuyển của tài xế vào doanh thu bán trà)
                BigDecimal netAmount = itemTotal.subtract(discount);
                if (netAmount.compareTo(BigDecimal.ZERO) < 0) {
                    netAmount = BigDecimal.ZERO;
                }

                totalGrossRevenue = totalGrossRevenue.add(itemTotal);
                totalDiscountAmount = totalDiscountAmount.add(discount);
                totalNetRevenue = totalNetRevenue.add(netAmount);
            }
        }

        BigDecimal aov = BigDecimal.ZERO;
        if (completedOrders > 0) {
            aov = totalNetRevenue.divide(BigDecimal.valueOf(completedOrders), 0, RoundingMode.HALF_UP);
        }

        // 4.2. Tính toán tổng chi tiêu & lợi nhuận
        BigDecimal totalExpense = BigDecimal.ZERO;
        long totalImportReceipts = importReceipts.size();
        for (com.ute.trasua.entity.ImportReceipt ir : importReceipts) {
            if (ir.getTotalAmount() != null) {
                totalExpense = totalExpense.add(ir.getTotalAmount());
            }
        }

        BigDecimal totalProfit = totalNetRevenue.subtract(totalExpense);
        double profitMargin = 0.0;
        if (totalNetRevenue.compareTo(BigDecimal.ZERO) > 0) {
            profitMargin = totalProfit.multiply(BigDecimal.valueOf(100))
                    .divide(totalNetRevenue, 1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        // 5. Thống kê theo ngày liên tục (Daily timeline) bao gồm cả chi tiêu và lợi nhuận
        List<RevenueReportResponse.DailyRevenueDto> dailyList = buildDailyRevenueList(startDate, endDate, paidOrders, importReceipts);

        // 6. Thống kê theo từng chi nhánh (Branch breakdown) bao gồm cả chi tiêu và lợi nhuận
        List<RevenueReportResponse.BranchRevenueDto> branchList = buildBranchRevenueList(branchId, paidOrders, totalNetRevenue, importReceipts);

        // 7. Thống kê theo phương thức thanh toán
        List<RevenueReportResponse.PaymentMethodRevenueDto> paymentList = buildPaymentMethodList(paidOrders, totalNetRevenue);

        // 8. Top 10 sản phẩm bán chạy nhất
        List<RevenueReportResponse.TopProductDto> topProductList = buildTopProductsList(branchId, startDateTime, endDateTime);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDateRange = String.format("Từ %s đến %s", startDate.format(dtf), endDate.format(dtf));

        return RevenueReportResponse.builder()
                .branchId(branchId)
                .branchName(branchName)
                .branchCode(branchCode)
                .startDate(startDate)
                .endDate(endDate)
                .period(request.getPeriod())
                .formattedDateRange(formattedDateRange)
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .totalGrossRevenue(totalGrossRevenue)
                .formattedTotalGrossRevenue(CURRENCY_FORMAT.format(totalGrossRevenue))
                .totalDiscountAmount(totalDiscountAmount)
                .formattedTotalDiscountAmount(CURRENCY_FORMAT.format(totalDiscountAmount))
                .totalNetRevenue(totalNetRevenue)
                .formattedTotalNetRevenue(CURRENCY_FORMAT.format(totalNetRevenue))
                .averageOrderValue(aov)
                .formattedAverageOrderValue(CURRENCY_FORMAT.format(aov))
                .totalExpense(totalExpense)
                .formattedTotalExpense(CURRENCY_FORMAT.format(totalExpense))
                .totalProfit(totalProfit)
                .formattedTotalProfit(CURRENCY_FORMAT.format(totalProfit))
                .profitMargin(profitMargin)
                .formattedProfitMargin(PERCENT_FORMAT.format(profitMargin))
                .totalImportReceipts(totalImportReceipts)
                .dailyRevenues(dailyList)
                .branchRevenues(branchList)
                .paymentMethods(paymentList)
                .topProducts(topProductList)
                .build();
    }

    /**
     * Xác định khoảng ngày bắt đầu - kết thúc dựa trên period hoặc ngày truyền vào.
     */
    private LocalDate[] resolveDateRange(RevenueReportRequest request) {
        String period = (request.getPeriod() != null) ? request.getPeriod().trim().toUpperCase() : "THIS_MONTH";
        LocalDate today = LocalDate.now();

        LocalDate start;
        LocalDate end;

        switch (period) {
            case "TODAY":
                start = today;
                end = today;
                break;
            case "YESTERDAY":
                start = today.minusDays(1);
                end = today.minusDays(1);
                break;
            case "THIS_WEEK":
                start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                end = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                break;
            case "LAST_MONTH":
                LocalDate lastMonth = today.minusMonths(1);
                start = lastMonth.with(TemporalAdjusters.firstDayOfMonth());
                end = lastMonth.with(TemporalAdjusters.lastDayOfMonth());
                break;
            case "CUSTOM":
                start = (request.getStartDate() != null) ? request.getStartDate() : today.with(TemporalAdjusters.firstDayOfMonth());
                end = (request.getEndDate() != null) ? request.getEndDate() : today;
                if (start.isAfter(end)) {
                    LocalDate temp = start;
                    start = end;
                    end = temp;
                }
                break;
            case "THIS_MONTH":
            default:
                start = today.with(TemporalAdjusters.firstDayOfMonth());
                end = today.with(TemporalAdjusters.lastDayOfMonth());
                break;
        }

        return new LocalDate[]{start, end};
    }

    /**
     * Xây dựng danh sách doanh thu, chi tiêu và lợi nhuận theo ngày liên tục để vẽ biểu đồ và phân tích xu hướng.
     */
    private List<RevenueReportResponse.DailyRevenueDto> buildDailyRevenueList(LocalDate startDate, LocalDate endDate,
                                                                              List<Order> paidOrders,
                                                                              List<com.ute.trasua.entity.ImportReceipt> importReceipts) {
        Map<LocalDate, List<Order>> ordersByDate = paidOrders.stream()
                .filter(o -> o.getCreatedAt() != null)
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));

        Map<LocalDate, List<com.ute.trasua.entity.ImportReceipt>> receiptsByDate = importReceipts.stream()
                .filter(r -> r.getImportDate() != null)
                .collect(Collectors.groupingBy(r -> r.getImportDate().toLocalDate()));

        List<RevenueReportResponse.DailyRevenueDto> list = new ArrayList<>();
        LocalDate curr = startDate;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");
        String[] dayNames = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};

        while (!curr.isAfter(endDate)) {
            List<Order> dayOrders = ordersByDate.getOrDefault(curr, Collections.emptyList());

            long count = dayOrders.size();
            BigDecimal gross = BigDecimal.ZERO;
            BigDecimal discount = BigDecimal.ZERO;
            BigDecimal net = BigDecimal.ZERO;

            for (Order o : dayOrders) {
                BigDecimal it = (o.getTotalAmount() != null) ? o.getTotalAmount() : BigDecimal.ZERO;
                BigDecimal dc = (o.getDiscountAmount() != null) ? o.getDiscountAmount() : BigDecimal.ZERO;
                BigDecimal nt = it.subtract(dc);
                if (nt.compareTo(BigDecimal.ZERO) < 0) {
                    nt = BigDecimal.ZERO;
                }

                gross = gross.add(it);
                discount = discount.add(dc);
                net = net.add(nt);
            }

            // Chi tiêu nhập kho trong ngày
            List<com.ute.trasua.entity.ImportReceipt> dayReceipts = receiptsByDate.getOrDefault(curr, Collections.emptyList());
            BigDecimal dayExpense = BigDecimal.ZERO;
            for (com.ute.trasua.entity.ImportReceipt r : dayReceipts) {
                if (r.getTotalAmount() != null) {
                    dayExpense = dayExpense.add(r.getTotalAmount());
                }
            }
            BigDecimal dayProfit = net.subtract(dayExpense);

            int dayOfWeekIdx = curr.getDayOfWeek().getValue() - 1;
            String dayName = (dayOfWeekIdx >= 0 && dayOfWeekIdx < dayNames.length) ? dayNames[dayOfWeekIdx] : "";

            list.add(RevenueReportResponse.DailyRevenueDto.builder()
                    .dateStr(curr.toString())
                    .formattedDate(curr.format(dtf))
                    .dayOfWeek(dayName)
                    .orderCount(count)
                    .grossAmount(gross)
                    .discountAmount(discount)
                    .netAmount(net)
                    .formattedNetAmount(CURRENCY_FORMAT.format(net))
                    .expenseAmount(dayExpense)
                    .formattedExpenseAmount(CURRENCY_FORMAT.format(dayExpense))
                    .profitAmount(dayProfit)
                    .formattedProfitAmount(CURRENCY_FORMAT.format(dayProfit))
                    .build());

            curr = curr.plusDays(1);
        }

        return list;
    }

    /**
     * Xây dựng danh sách doanh thu, chi tiêu và lợi nhuận theo từng chi nhánh và tính tỷ trọng phần trăm.
     */
    private List<RevenueReportResponse.BranchRevenueDto> buildBranchRevenueList(Long branchId,
                                                                                List<Order> paidOrders,
                                                                                BigDecimal totalNetRevenue,
                                                                                List<com.ute.trasua.entity.ImportReceipt> importReceipts) {
        List<Branch> branches = branchRepository.findAll();
        Map<Long, List<Order>> ordersByBranch = paidOrders.stream()
                .filter(o -> o.getBranch() != null)
                .collect(Collectors.groupingBy(o -> o.getBranch().getId()));

        Map<Long, List<com.ute.trasua.entity.ImportReceipt>> receiptsByBranch = importReceipts.stream()
                .filter(r -> r.getBranch() != null)
                .collect(Collectors.groupingBy(r -> r.getBranch().getId()));

        List<RevenueReportResponse.BranchRevenueDto> list = new ArrayList<>();

        for (Branch b : branches) {
            if (branchId != null && !b.getId().equals(branchId)) {
                continue;
            }

            List<Order> bOrders = ordersByBranch.getOrDefault(b.getId(), Collections.emptyList());
            long count = bOrders.size();
            BigDecimal net = bOrders.stream()
                    .map(o -> {
                        BigDecimal it = (o.getTotalAmount() != null) ? o.getTotalAmount() : BigDecimal.ZERO;
                        BigDecimal dc = (o.getDiscountAmount() != null) ? o.getDiscountAmount() : BigDecimal.ZERO;
                        BigDecimal nt = it.subtract(dc);
                        return nt.compareTo(BigDecimal.ZERO) > 0 ? nt : BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double percentage = 0.0;
            if (totalNetRevenue.compareTo(BigDecimal.ZERO) > 0) {
                percentage = net.multiply(BigDecimal.valueOf(100))
                        .divide(totalNetRevenue, 1, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            // Chi tiêu và lợi nhuận chi nhánh
            List<com.ute.trasua.entity.ImportReceipt> bReceipts = receiptsByBranch.getOrDefault(b.getId(), Collections.emptyList());
            BigDecimal bExpense = bReceipts.stream()
                    .map(r -> r.getTotalAmount() != null ? r.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal bProfit = net.subtract(bExpense);
            double bMargin = 0.0;
            if (net.compareTo(BigDecimal.ZERO) > 0) {
                bMargin = bProfit.multiply(BigDecimal.valueOf(100))
                        .divide(net, 1, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            list.add(RevenueReportResponse.BranchRevenueDto.builder()
                    .branchId(b.getId())
                    .branchCode(b.getBranchCode())
                    .branchName(b.getName())
                    .orderCount(count)
                    .netAmount(net)
                    .formattedNetAmount(CURRENCY_FORMAT.format(net))
                    .percentage(percentage)
                    .expenseAmount(bExpense)
                    .formattedExpenseAmount(CURRENCY_FORMAT.format(bExpense))
                    .profitAmount(bProfit)
                    .formattedProfitAmount(CURRENCY_FORMAT.format(bProfit))
                    .profitMargin(bMargin)
                    .build());
        }

        list.sort((a, b) -> b.getNetAmount().compareTo(a.getNetAmount()));
        return list;
    }

    /**
     * Thống kê theo phương thức thanh toán.
     */
    private List<RevenueReportResponse.PaymentMethodRevenueDto> buildPaymentMethodList(List<Order> paidOrders, BigDecimal totalNetRevenue) {
        Map<String, List<Order>> ordersByMethod = paidOrders.stream()
                .collect(Collectors.groupingBy(o -> {
                    String pm = o.getPaymentMethod();
                    return (pm != null && !pm.isBlank()) ? pm.toUpperCase() : "CASH";
                }));

        List<RevenueReportResponse.PaymentMethodRevenueDto> list = new ArrayList<>();

        for (Map.Entry<String, List<Order>> entry : ordersByMethod.entrySet()) {
            String methodKey = entry.getKey();
            List<Order> methodOrders = entry.getValue();

            long count = methodOrders.size();
            BigDecimal net = methodOrders.stream()
                    .map(o -> {
                        BigDecimal it = (o.getTotalAmount() != null) ? o.getTotalAmount() : BigDecimal.ZERO;
                        BigDecimal dc = (o.getDiscountAmount() != null) ? o.getDiscountAmount() : BigDecimal.ZERO;
                        BigDecimal nt = it.subtract(dc);
                        return nt.compareTo(BigDecimal.ZERO) > 0 ? nt : BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double percentage = 0.0;
            if (totalNetRevenue.compareTo(BigDecimal.ZERO) > 0) {
                percentage = net.multiply(BigDecimal.valueOf(100))
                        .divide(totalNetRevenue, 1, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            String methodName = switch (methodKey) {
                case "CASH" -> "Tiền mặt (POS)";
                case "BANK_TRANSFER", "VIETQR" -> "Chuyển khoản VietQR";
                case "PAYOS" -> "Cổng PayOS (VietQR / Thẻ)";
                case "MOMO" -> "Ví điện tử MoMo";
                case "VNPAY" -> "Cổng VNPAY";
                default -> methodKey;
            };

            list.add(RevenueReportResponse.PaymentMethodRevenueDto.builder()
                    .paymentMethod(methodKey)
                    .paymentMethodName(methodName)
                    .orderCount(count)
                    .netAmount(net)
                    .formattedNetAmount(CURRENCY_FORMAT.format(net))
                    .percentage(percentage)
                    .build());
        }

        list.sort((a, b) -> b.getNetAmount().compareTo(a.getNetAmount()));
        return list;
    }

    /**
     * Top 10 món bán chạy nhất theo số lượng bán.
     */
    private List<RevenueReportResponse.TopProductDto> buildTopProductsList(Long branchId, LocalDateTime start, LocalDateTime end) {
        List<Object[]> rawList = orderRepository.findTopSellingProducts(branchId, start, end);
        List<RevenueReportResponse.TopProductDto> list = new ArrayList<>();

        int count = 0;
        for (Object[] row : rawList) {
            if (count++ >= 10) break;

            Long prodId = (row[0] != null) ? ((Number) row[0]).longValue() : null;
            String prodName = (row[1] != null) ? row[1].toString() : "Món nước";
            long qty = (row[2] != null) ? ((Number) row[2]).longValue() : 0L;
            BigDecimal rev = (row[3] != null) ? (BigDecimal) row[3] : BigDecimal.ZERO;

            list.add(RevenueReportResponse.TopProductDto.builder()
                    .productId(prodId)
                    .productName(prodName)
                    .categoryName("Trà Sữa & Topping")
                    .quantitySold(qty)
                    .totalRevenue(rev)
                    .formattedTotalRevenue(CURRENCY_FORMAT.format(rev))
                    .build());
        }

        return list;
    }

    /**
     * Xuất file Excel chuẩn biểu mẫu BM1 (Module 15 / UC30).
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] exportRevenueExcelBM1(RevenueReportRequest request) {
        RevenueReportResponse report = generateRevenueReport(request);
        return excelExportService.exportRevenueReportBM1(report);
    }
}
