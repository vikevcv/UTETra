package com.ute.trasua.service;

import com.ute.trasua.dto.driver.DriverPerformanceSummaryDto;
import com.ute.trasua.dto.driver.DriverTripHistoryDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Service trích xuất báo cáo lịch sử cuốc giao và hiệu suất làm việc của tài xế (UC15).
 */
public interface DriverReportService {

    /**
     * Lấy danh sách lịch sử các cuốc giao (thành công / hoàn trả) của tài xế.
     */
    List<DriverTripHistoryDto> getDriverCompletedTrips(Long driverId);

    /**
     * Tổng hợp chỉ số hiệu suất & KPI của tài xế theo khoảng thời gian.
     */
    DriverPerformanceSummaryDto getDriverPerformanceSummary(Long driverId, LocalDate fromDate, LocalDate toDate);
}
