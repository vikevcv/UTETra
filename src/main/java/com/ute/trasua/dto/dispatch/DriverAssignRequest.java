package com.ute.trasua.dto.dispatch;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO yêu cầu phân công đơn hàng cho tài xế từ Quản lý chi nhánh.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverAssignRequest {

    @NotNull(message = "Thiếu ID tài xế")
    private Long driverId;

    private String note;
}
