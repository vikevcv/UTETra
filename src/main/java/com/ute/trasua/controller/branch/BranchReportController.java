package com.ute.trasua.controller.branch;

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
import org.springframework.security.core.Authentication;
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
 * Controller xử lý báo cáo doanh thu chi nhánh và tích hợp biểu đồ Chart.js (Module 15 / UC19).
 */
@Controller
@RequestMapping("/branch/reports")
@Slf4j
public class BranchReportController {

    private final RevenueReportService revenueReportService;
    private final BranchService branchService;

    public BranchReportController(RevenueReportService revenueReportService, BranchService branchService) {
        this.revenueReportService = revenueReportService;
        this.branchService = branchService;
    }

    /**
     * Hiển thị Dashboard báo cáo doanh thu chi nhánh với đồ thị Chart.js (UC19).
     */
    @GetMapping
    public String showRevenueDashboard(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "period", defaultValue = "THIS_MONTH") String period,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            Authentication authentication,
            Model model
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        Long targetBranchId = (currentBranch != null) ? currentBranch.getId() : null;

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()) || "ADMIN".equalsIgnoreCase(a.getAuthority()));
        List<BranchResponse> allBranches = isAdmin ? branchService.getAllBranches()
                : (currentBranch != null ? List.of(currentBranch) : List.of());

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
                .branchId(targetBranchId)
                .period(period)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        RevenueReportResponse report = revenueReportService.generateRevenueReport(request);

        // Chuẩn bị dữ liệu mảng JSON cho Chart.js
        List<String> labels = new ArrayList<>();
        List<Double> netRevenues = new ArrayList<>();
        List<Double> grossRevenues = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();

        if (report.getDailyRevenues() != null) {
            for (RevenueReportResponse.DailyRevenueDto d : report.getDailyRevenues()) {
                labels.add(d.getFormattedDate() + " (" + d.getDayOfWeek() + ")");
                netRevenues.add(d.getNetAmount() != null ? d.getNetAmount().doubleValue() : 0.0);
                grossRevenues.add(d.getGrossAmount() != null ? d.getGrossAmount().doubleValue() : 0.0);
                orderCounts.add(d.getOrderCount());
            }
        }

        List<String> paymentLabels = new ArrayList<>();
        List<Double> paymentValues = new ArrayList<>();
        if (report.getPaymentMethods() != null) {
            for (RevenueReportResponse.PaymentMethodRevenueDto pm : report.getPaymentMethods()) {
                paymentLabels.add(pm.getPaymentMethodName());
                paymentValues.add(pm.getNetAmount() != null ? pm.getNetAmount().doubleValue() : 0.0);
            }
        }

        model.addAttribute("chartLabelsJson", toJsonArray(labels));
        model.addAttribute("chartNetRevenueJson", toJsonArray(netRevenues));
        model.addAttribute("chartGrossRevenueJson", toJsonArray(grossRevenues));
        model.addAttribute("chartOrderCountJson", toJsonArray(orderCounts));
        model.addAttribute("chartPaymentLabelsJson", toJsonArray(paymentLabels));
        model.addAttribute("chartPaymentValuesJson", toJsonArray(paymentValues));

        model.addAttribute("report", report);
        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("allBranches", allBranches);
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        model.addAttribute("pageTitle", "Báo Cáo Doanh Thu Chi Nhánh (UC19)");

        return "branch/reports/dashboard";
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

    /**
     * Xuất báo cáo doanh thu ra tệp Excel (.xlsx) chuẩn biểu mẫu BM1 (QLCN_BM1).
     */
    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcelBM1(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "period", defaultValue = "THIS_MONTH") String period,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            Authentication authentication
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        Long targetBranchId = (currentBranch != null) ? currentBranch.getId() : null;

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
                .branchId(targetBranchId)
                .period(period)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        byte[] excelData = revenueReportService.exportRevenueExcelBM1(request);

        String branchCode = (currentBranch != null) ? currentBranch.getBranchCode() : "ALL";
        String dateSuffix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = String.format("BaoCao_DoanhThu_BM1_%s_%s.xlsx", branchCode, dateSuffix);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    private BranchResponse resolveCurrentBranch(Long requestedBranchId, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()) || "ADMIN".equalsIgnoreCase(a.getAuthority()));
            if (!isAdmin) {
                String username = authentication.getName();
                return branchService.getBranchByManagerUsername(username).orElse(null);
            }
        }

        if (requestedBranchId != null) {
            try {
                return branchService.getBranchById(requestedBranchId);
            } catch (Exception ignored) {}
        }

        List<BranchResponse> all = branchService.getAllBranches();
        return all.isEmpty() ? null : all.get(0);
    }
}
