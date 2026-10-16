package com.ute.trasua.dto.handover;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO yêu cầu thao tác bàn giao đơn hàng cho tài xế hoặc xử lý đơn.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandoverActionRequest {

    private Long orderId;
    private Long driverId;
    private String notes;
}
