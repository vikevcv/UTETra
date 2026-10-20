package com.ute.trasua.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Real-time event payload pushed through WebSocket STOMP topics (Module 3 - UC05, UC11, UC13).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusEventDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Event type: ORDER_CREATED, PREPARING, PREPARED, DISPATCHED, DELIVERING, COMPLETED, CANCELLED, LOCATION_UPDATE
     */
    private String eventType;

    private Long orderId;
    private String orderCode;
    private Long branchId;
    private String branchName;

    private Integer dailyQueueNumber;
    private String status;         // PENDING, PREPARING, DELIVERING, COMPLETED, CANCELLED
    private String statusLabel;    // Friendly Vietnamese status label (Đang Pha Chế, Đang Giao Hàng, ...)
    private Integer statusStep;    // 1: Placed, 2: Brewing, 3: Delivering, 4: Completed, -1: Cancelled

    private String statusTitle;    // Friendly Vietnamese title
    private String statusMessage;  // Detailed message for customer notification

    // Driver & vehicle details (populated when assigned or updated)
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private String vehiclePlate;
    private Double driverLatitude;
    private Double driverLongitude;

    private String timestamp;
}
