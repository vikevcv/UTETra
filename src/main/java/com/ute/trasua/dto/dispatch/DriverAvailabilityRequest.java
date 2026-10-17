package com.ute.trasua.dto.dispatch;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cập nhật trạng thái trực ca của tài xế (Bật / Tắt ca ONLINE).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverAvailabilityRequest {

    @NotNull(message = "Thiếu trạng thái ca trực")
    private Boolean available;
}
