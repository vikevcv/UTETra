package com.ute.trasua.dto.dispatch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO yêu cầu báo cáo sự cố bất khả kháng từ tài xế (UC12 / UC14).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentReportRequest {

    @NotBlank(message = "Mã lý do sự cố không được để trống")
    private String reasonCode; // XE_HONG | SUC_KHOE | KET_DON | KHACH_HUY

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String note;
}
