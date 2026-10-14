package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO request tiêu chí lọc báo cáo doanh thu chuỗi & chi nhánh (Module 15 / UC30).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportRequest {

    /**
     * ID chi nhánh cần lọc (null = Toàn bộ chuỗi UTETra)
     */
    private Long branchId;

    /**
     * Khoảng thời gian định sẵn: TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, LAST_MONTH, CUSTOM
     */
    @Builder.Default
    private String period = "THIS_MONTH";

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}
