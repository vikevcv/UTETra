package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO response chứa toàn bộ số liệu thống kê doanh thu và báo cáo tổng hợp (Module 15 / UC30).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportResponse {

    private Long branchId;
    private String branchName;
    private String branchCode;

    private LocalDate startDate;
    private LocalDate endDate;
    private String formattedDateRange;
    private String period;

    // KPI TỔNG QUAN DOANH THU
    private long totalOrders;
    private long completedOrders;
    private long cancelledOrders;

    /** Tiền món (Gross Revenue) */
    @Builder.Default
    private BigDecimal totalGrossRevenue = BigDecimal.ZERO;
    private String formattedTotalGrossRevenue;

    /** Tiền giảm giá Voucher */
    @Builder.Default
    private BigDecimal totalDiscountAmount = BigDecimal.ZERO;
    private String formattedTotalDiscountAmount;

    /** Thực thu: Thực thu = Tiền món + Phí vận chuyển - Voucher */
    @Builder.Default
    private BigDecimal totalNetRevenue = BigDecimal.ZERO;
    private String formattedTotalNetRevenue;

    /** Giá trị trung bình mỗi đơn (AOV) */
    @Builder.Default
    private BigDecimal averageOrderValue = BigDecimal.ZERO;
    private String formattedAverageOrderValue;

    // THỐNG KÊ CHI TIÊU & LỢI NHUẬN
    /** Tổng chi tiêu nhập kho nguyên liệu (BM2) */
    @Builder.Default
    private BigDecimal totalExpense = BigDecimal.ZERO;
    private String formattedTotalExpense;

    /** Lợi nhuận gộp hoạt động: Lợi nhuận = Thực thu - Chi tiêu nhập kho */
    @Builder.Default
    private BigDecimal totalProfit = BigDecimal.ZERO;
    private String formattedTotalProfit;

    /** Tỷ suất lợi nhuận (%): (Lợi nhuận / Thực thu) * 100 */
    @Builder.Default
    private double profitMargin = 0.0;
    private String formattedProfitMargin;

    /** Số phiếu nhập kho trong kỳ */
    private long totalImportReceipts;

    // DỮ LIỆU ĐỒ THỊ & BẢNG BIỂU
    @Builder.Default
    private List<DailyRevenueDto> dailyRevenues = new ArrayList<>();

    @Builder.Default
    private List<BranchRevenueDto> branchRevenues = new ArrayList<>();

    @Builder.Default
    private List<TopProductDto> topProducts = new ArrayList<>();

    @Builder.Default
    private List<PaymentMethodRevenueDto> paymentMethods = new ArrayList<>();

    /**
     * Doanh thu, chi tiêu và lợi nhuận theo từng ngày trong chuỗi thời gian (cho Chart.js line / bar chart).
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyRevenueDto {
        private String dateStr; // yyyy-MM-dd
        private String formattedDate; // dd/MM
        private String dayOfWeek;
        private long orderCount;
        private BigDecimal grossAmount;
        private BigDecimal discountAmount;
        private BigDecimal netAmount;
        private String formattedNetAmount;

        /** Chi tiêu nhập kho trong ngày */
        @Builder.Default
        private BigDecimal expenseAmount = BigDecimal.ZERO;
        private String formattedExpenseAmount;

        /** Lợi nhuận trong ngày: netAmount - expenseAmount */
        @Builder.Default
        private BigDecimal profitAmount = BigDecimal.ZERO;
        private String formattedProfitAmount;
    }

    /**
     * Doanh thu, chi tiêu và lợi nhuận theo từng chi nhánh (so sánh tỷ trọng toàn chuỗi).
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BranchRevenueDto {
        private Long branchId;
        private String branchCode;
        private String branchName;
        private long orderCount;
        private BigDecimal netAmount;
        private String formattedNetAmount;
        private double percentage; // % đóng góp doanh thu

        /** Tổng thu trước giảm giá / doanh thu gộp */
        @Builder.Default
        private BigDecimal grossAmount = BigDecimal.ZERO;
        private String formattedGrossAmount;

        /** Tiền chiết khấu / voucher của chi nhánh */
        @Builder.Default
        private BigDecimal discountAmount = BigDecimal.ZERO;
        private String formattedDiscountAmount;

        /** Chi tiêu nhập kho của chi nhánh */
        @Builder.Default
        private BigDecimal expenseAmount = BigDecimal.ZERO;
        private String formattedExpenseAmount;

        /** Lợi nhuận của chi nhánh */
        @Builder.Default
        private BigDecimal profitAmount = BigDecimal.ZERO;
        private String formattedProfitAmount;

        /** Tỷ suất lợi nhuận của chi nhánh (%) */
        @Builder.Default
        private double profitMargin = 0.0;
    }

    /**
     * Top sản phẩm bán chạy nhất.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProductDto {
        private Long productId;
        private String productName;
        private String categoryName;
        private long quantitySold;
        private BigDecimal totalRevenue;
        private String formattedTotalRevenue;
    }

    /**
     * Thống kê theo phương thức thanh toán (Tiền mặt, Chuyển khoản VietQR...).
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentMethodRevenueDto {
        private String paymentMethod;
        private String paymentMethodName;
        private long orderCount;
        private BigDecimal netAmount;
        private String formattedNetAmount;
        private double percentage;
    }
}
