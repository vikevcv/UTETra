package com.ute.trasua.service;

import com.ute.trasua.dto.RevenueReportRequest;
import com.ute.trasua.dto.RevenueReportResponse;

/**
 * Service interface xử lý báo cáo thống kê doanh thu chuỗi & chi nhánh (Module 15 / UC30).
 * Tuân thủ quy chuẩn phân tầng Interface & Implementation.
 */
public interface RevenueReportService {

    /**
     * Tạo báo cáo doanh thu chi tiết theo tiêu chí lọc (thời gian, chi nhánh).
     * Áp dụng công thức chuẩn: Thực thu = Tiền món + Phí vận chuyển - Voucher.
     */
    RevenueReportResponse generateRevenueReport(RevenueReportRequest request);

    /**
     * Xuất báo cáo doanh thu ra file Excel chuẩn biểu mẫu BM1 (Apache POI).
     */
    byte[] exportRevenueExcelBM1(RevenueReportRequest request);

    /**
     * Xuất báo cáo xếp hạng hiệu quả chi nhánh (thu, chi voucher, chi phí, lợi nhuận) ra Excel.
     */
    byte[] exportBranchRankingExcel(RevenueReportRequest request);

    /**
     * Xuất báo cáo doanh thu, chi tiêu và lợi nhuận theo ngày ra Excel.
     */
    byte[] exportDailyRevenueExcel(RevenueReportRequest request);
}
