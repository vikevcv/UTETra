package com.ute.trasua.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO hiển thị chi tiết lịch sử cuốc giao hàng của Shipper.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDriverTripDto {

    private Long orderId;
    private String orderCode;
    private String branchName;
    private String branchCode;

    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private BigDecimal distanceKm;

    private int cupCount;
    private BigDecimal shippingFee;
    private String formattedShippingFee;
    private BigDecimal finalAmount;
    private String formattedFinalAmount;

    private String status;
    private String statusDisplay;
    private String statusBadgeClass;

    private LocalDateTime createdAt;
    private String formattedCreatedAt;
    private LocalDateTime handedOverAt;
    private String formattedHandedOverAt;
    private LocalDateTime deliveredAt;
    private String formattedDeliveredAt;

    private String pickupPhotoUrl;
    private String deliveryPhotoUrl;
    private String incidentReason;
    private String incidentNote;
    private String failureReason;
    private String failureProofUrl;
}
