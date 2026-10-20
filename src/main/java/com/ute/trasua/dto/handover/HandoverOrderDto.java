package com.ute.trasua.dto.handover;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO hiển thị danh sách đơn hàng chờ bàn giao, đang giao và lịch sử giao hàng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandoverOrderDto {

    private Long orderId;
    private String orderCode;
    private Integer dailyQueueNumber;
    private String orderType;
    private String status;
    private String statusLabel;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private Double latitude;
    private Double longitude;
    private String deliveryNotes;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private Integer cupCount;
    private String itemsSummary;
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private String vehiclePlate;
    private LocalDateTime createdAt;
    private LocalDateTime handedOverAt;
    private LocalDateTime deliveredAt;
    private String incidentReason;
    private String incidentNote;
    private LocalDateTime incidentReportedAt;
    private String pickupPhotoUrl;
    private String deliveryPhotoUrl;
    private String failureReason;
    private String failureProofUrl;
    private LocalDateTime returnedAt;
}
