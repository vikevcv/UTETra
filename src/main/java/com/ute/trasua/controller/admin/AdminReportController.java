package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.BranchResponse;
import com.ute.trasua.dto.RevenueReportRequest;
import com.ute.trasua.dto.RevenueReportResponse;
import com.ute.trasua.service.BranchService;
import com.ute.trasua.service.RevenueReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller xử lý báo cáo so sánh doanh thu toàn chuỗi và phân tích hiệu quả các chi nhánh (Module 15 / UC26).
 */
@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@Slf4j
public class AdminReportController {

    private final RevenueReportService revenueReportService;
    private final BranchService branchService;

    /**
     * Hiển thị Dashboard so sánh doanh thu toàn chuỗi với các biểu đồ Chart.js (UC26).
     */
    @GetMapping
    public String showChainReportDashboard(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "period", defaultValue = "THIS_MONTH") String period,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            Model model
    ) {
        List<BranchResponse> allBranches = branchService.getAllBranches();

        BranchResponse selectedBranch = null;
        if (branchId != null) {
            try {
                selectedBranch = branchService.getBranchById(branchId);
            } catch (Exception ignored) {
                branchId = null;
            }
        }

        LocalDate startDate = null;
        if (startDateStr != null && !startDateStr.trim().isEmpty()) {
            try {
                startDate = LocalDate.parse(startDateStr.trim());
            } catch (Exception ignored) {}
        }

        LocalDate endDate = null;
        if (endDateStr != null && !endDateStr.trim().isEmpty()) {
            try {
                endDate = LocalDate.parse(endDateStr.trim());
            } catch (Exception ignored) {}
        }

        RevenueReportRequest request = RevenueReportRequest.builder()
                .branchId(branchId)
                .period(period)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        RevenueReportResponse report = revenueReportService.generateRevenueReport(request);

        // 1. Dữ liệu xu hướng doanh thu, chi tiêu và lợi nhuận theo ngày (Timeline Chart)
        List<String> timelineLabels = new ArrayList<>();
        List<Double> timelineNetRevenues = new ArrayList<>();
        List<Double> timelineGrossRevenues = new ArrayList<>();
        List<Double> timelineExpenses = new ArrayList<>();
        List<Double> timelineProfits = new ArrayList<>();
        List<Long> timelineOrderCounts = new ArrayList<>();

        if (report.getDailyRevenues() != null) {
            for (RevenueReportResponse.DailyRevenueDto d : report.getDailyRevenues()) {
                timelineLabels.add(d.getFormattedDate() + " (" + d.getDayOfWeek() + ")");
                timelineNetRevenues.add(d.getNetAmount() != null ? d.getNetAmount().doubleValue() : 0.0);
                timelineGrossRevenues.add(d.getGrossAmount() != null ? d.getGrossAmount().doubleValue() : 0.0);
                timelineExpenses.add(d.getExpenseAmount() != null ? d.getExpenseAmount().doubleValue() : 0.0);
                timelineProfits.add(d.getProfitAmount() != null ? d.getProfitAmount().doubleValue() : 0.0);
                timelineOrderCounts.add(d.getOrderCount());
            }
        }

        // 2. Dữ liệu so sánh hiệu quả các chi nhánh (Branch Comparison Bar & Pie Chart)
        List<String> branchLabels = new ArrayList<>();
        List<Double> branchRevenues = new ArrayList<>();
        List<Double> branchExpenses = new ArrayList<>();
        List<Double> branchProfits = new ArrayList<>();
        List<Long> branchOrderCounts = new ArrayList<>();
        List<Double> branchPercentages = new ArrayList<>();

        if (report.getBranchRevenues() != null) {
            for (RevenueReportResponse.BranchRevenueDto br : report.getBranchRevenues()) {
                branchLabels.add(br.getBranchName());
                branchRevenues.add(br.getNetAmount() != null ? br.getNetAmount().doubleValue() : 0.0);
                branchExpenses.add(br.getExpenseAmount() != null ? br.getExpenseAmount().doubleValue() : 0.0);
                branchProfits.add(br.getProfitAmount() != null ? br.getProfitAmount().doubleValue() : 0.0);
                branchOrderCounts.add(br.getOrderCount());
                branchPercentages.add(br.getPercentage());
            }
        }

        // 3. Dữ liệu phương thức thanh toán (Payment Method Chart)
        List<String> paymentLabels = new ArrayList<>();
        List<Double> paymentValues = new ArrayList<>();
        if (report.getPaymentMethods() != null) {
            for (RevenueReportResponse.PaymentMethodRevenueDto pm : report.getPaymentMethods()) {
                paymentLabels.add(pm.getPaymentMethodName());
                paymentValues.add(pm.getNetAmount() != null ? pm.getNetAmount().doubleValue() : 0.0);
            }
        }

        // Chuẩn hóa JSON cho view
        model.addAttribute("chartTimelineLabelsJson", toJsonArray(timelineLabels));
        model.addAttribute("chartTimelineNetJson", toJsonArray(timelineNetRevenues));
        model.addAttribute("chartTimelineGrossJson", toJsonArray(timelineGrossRevenues));
        model.addAttribute("chartTimelineExpensesJson", toJsonArray(timelineExpenses));
        model.addAttribute("chartTimelineProfitsJson", toJsonArray(timelineProfits));
        model.addAttribute("chartTimelineOrdersJson", toJsonArray(timelineOrderCounts));

        model.addAttribute("chartBranchLabelsJson", toJsonArray(branchLabels));
        model.addAttribute("chartBranchRevenuesJson", toJsonArray(branchRevenues));
        model.addAttribute("chartBranchExpensesJson", toJsonArray(branchExpenses));
        model.addAttribute("chartBranchProfitsJson", toJsonArray(branchProfits));
        model.addAttribute("chartBranchOrdersJson", toJsonArray(branchOrderCounts));
        model.addAttribute("chartBranchPercentagesJson", toJsonArray(branchPercentages));

        model.addAttribute("chartPaymentLabelsJson", toJsonArray(paymentLabels));
        model.addAttribute("chartPaymentValuesJson", toJsonArray(paymentValues));

        model.addAttribute("report", report);
        model.addAttribute("allBranches", allBranches);
        model.addAttribute("selectedBranch", selectedBranch);
        model.addAttribute("selectedBranchId", branchId);
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        model.addAttribute("pageTitle", "Báo Cáo So Sánh Doanh Thu, Chi Tiêu & Lợi Nhuận Toàn Chuỗi (UC26)");

        return "admin/reports/dashboard";
    }

    /**
     * Xuất báo cáo doanh thu toàn chuỗi hoặc theo chi nhánh ra tệp Excel (.xlsx) chuẩn biểu mẫu BM1 (QLCN_BM1).
     */
    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcelBM1(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "period", defaultValue = "THIS_MONTH") String period,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr
    ) {
        RevenueReportRequest request = buildReportRequest(branchId, period, startDateStr, endDateStr);

        byte[] excelData = revenueReportService.exportRevenueExcelBM1(request);

        String branchSuffix = "TOAN_CHUOI";
        if (branchId != null) {
            try {
                BranchResponse b = branchService.getBranchById(branchId);
                branchSuffix = (b.getBranchCode() != null) ? b.getBranchCode() : "CN" + branchId;
            } catch (Exception ignored) {}
        }

        String dateSuffix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = String.format("BaoCao_DoanhThu_BM1_%s_%s.xlsx", branchSuffix, dateSuffix);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    /**
     * Xuất bảng xếp hạng và hiệu quả các chi nhánh (thu, chi voucher, chi phí, lợi nhuận) ra tệp Excel (.xlsx).
     */
    @GetMapping("/export-branches-excel")
    public ResponseEntity<byte[]> exportBranchRankingExcel(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "period", defaultValue = "THIS_MONTH") String period,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr
    ) {
        // Bảng xếp hạng chi nhánh luôn xuất toàn bộ tất cả chi nhánh trong chuỗi
        RevenueReportRequest request = buildReportRequest(null, period, startDateStr, endDateStr);
        byte[] excelData = revenueReportService.exportBranchRankingExcel(request);

        String dateSuffix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = String.format("BaoCao_XepHang_ChiNhanh_%s.xlsx", dateSuffix);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    /**
     * Xuất báo cáo doanh thu, chi tiêu kho và lợi nhuận theo ngày của toàn chuỗi ra tệp Excel (.xlsx).
     */
    @GetMapping("/export-daily-excel")
    public ResponseEntity<byte[]> exportDailyRevenueExcel(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "period", defaultValue = "THIS_MONTH") String period,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr
    ) {
        RevenueReportRequest request = buildReportRequest(branchId, period, startDateStr, endDateStr);
        byte[] excelData = revenueReportService.exportDailyRevenueExcel(request);

        String branchSuffix = "TOAN_CHUOI";
        if (branchId != null) {
            try {
                BranchResponse b = branchService.getBranchById(branchId);
                branchSuffix = (b.getBranchCode() != null) ? b.getBranchCode() : "CN" + branchId;
            } catch (Exception ignored) {}
        }

        String dateSuffix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = String.format("BaoCao_DoanhThu_TheoNgay_%s_%s.xlsx", branchSuffix, dateSuffix);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    private RevenueReportRequest buildReportRequest(Long branchId, String period, String startDateStr, String endDateStr) {
        LocalDate startDate = null;
        if (startDateStr != null && !startDateStr.trim().isEmpty()) {
            try {
                startDate = LocalDate.parse(startDateStr.trim());
            } catch (Exception ignored) {}
        }

        LocalDate endDate = null;
        if (endDateStr != null && !endDateStr.trim().isEmpty()) {
            try {
                endDate = LocalDate.parse(endDateStr.trim());
            } catch (Exception ignored) {}
        }

        return RevenueReportRequest.builder()
                .branchId(branchId)
                .period(period)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    private String toJsonArray(List<?> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (item instanceof Number) {
                sb.append(item);
            } else {
                sb.append("\"").append(item.toString().replace("\"", "\\\"")).append("\"");
            }
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
