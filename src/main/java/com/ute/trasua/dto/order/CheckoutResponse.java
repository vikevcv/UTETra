package com.ute.trasua.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response for online order creation (Module 3 - UC05).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutResponse {

    private boolean success;
    private String message;
    private Long orderId;
    private String orderCode;
    private Integer dailyQueueNumber;

    private Long branchId;
    private String branchName;
    private String branchAddress;

    private String customerName;
    private String customerPhone;
    private String deliveryAddress;

    private Double distanceKm;
    private BigDecimal shippingFee;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private String voucherCode;
    private BigDecimal finalAmount;

    private String paymentMethod;
    private String paymentStatus;
    private String paymentUrl;
}
