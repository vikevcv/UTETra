package com.ute.trasua.dto.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO tóm tắt hiệu suất giao hàng và chỉ số KPI cho Quản lý chi nhánh (UC15).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverPerformanceSummaryDto {

    private Long driverId;
    private String driverName;
    private String licensePlate;
    private Long branchId;
    private String branchName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer totalTrips;
    private Integer onTimeTrips;
    private Double onTimeRate;
    private Double kpiScore;
    private Integer completedOrders;
}
