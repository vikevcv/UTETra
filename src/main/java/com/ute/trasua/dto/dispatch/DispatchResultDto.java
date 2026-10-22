package com.ute.trasua.dto.dispatch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO kết quả phân công đơn hàng hoặc điều phối lại.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchResultDto {
    private Long orderId;
    private String orderCode;
    private String orderStatus;
    private Long driverId;
    private String driverName;
    private String driverWorkingStatus;
    private Integer driverQueueSize;
    private String message;
    private Boolean reassigned; // true nếu là điều phối lại sau sự cố
    private Boolean queued; // true nếu chưa có tài xế khả dụng, đơn đã dán tem và vào hàng đợi ghép
}
