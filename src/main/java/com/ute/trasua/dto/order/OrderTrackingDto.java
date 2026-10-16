package com.ute.trasua.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Detailed DTO for customer real-time order tracking and live map view (Module 3).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderTrackingDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String orderCode;
    private Integer dailyQueueNumber;
    private String orderType;

    /**
     * Stepper Status:
     * 1 = PENDING (Đã đặt hàng)
     * 2 = PREPARING / PROCESSING (Đang pha chế)
     * 3 = DISPATCHED / DELIVERING (Đang giao hàng)
     * 4 = COMPLETED (Đã giao thành công)
     * -1 = CANCELLED (Đã hủy)
     */
    private String status;
    private Integer statusStep;
    private String statusTitle;
    private String statusDescription;

    private String paymentMethod;
    private String paymentStatus;
    private String createdAt;

    // Customer & destination
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String deliveryNotes;
    private Double customerLatitude;
    private Double customerLongitude;

    // Serving branch
    private Long branchId;
    private String branchName;
    private String branchAddress;
    private String branchHotline;
    private Double branchLatitude;
    private Double branchLongitude;

    // Assigned driver & vehicle
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private String vehiclePlate;
    private Double driverLatitude;
    private Double driverLongitude;

    // Financial breakdown
    private BigDecimal distanceKm;
    private BigDecimal subtotalAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private String voucherCode;
    private BigDecimal finalAmount;

    @Builder.Default
    private List<OrderItemTrackingDto> items = new ArrayList<>();
}
