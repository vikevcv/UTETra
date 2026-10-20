package com.ute.trasua.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * DTO hiển thị chi tiết lịch sử đơn hàng mà User đã đặt mua.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderHistoryDto {

    private Long orderId;
    private String orderCode;
    private String branchName;
    private String branchCode;
    private LocalDateTime createdAt;
    private String formattedCreatedAt;

    private BigDecimal totalAmount;
    private String formattedTotalAmount;
    private BigDecimal discountAmount;
    private String formattedDiscountAmount;
    private BigDecimal shippingFee;
    private String formattedShippingFee;
    private BigDecimal finalAmount;
    private String formattedFinalAmount;

    private String orderType;
    private String orderTypeDisplay;
    private String paymentMethod;
    private String paymentMethodDisplay;
    private String paymentStatus;
    private String status;
    private String statusDisplay;
    private String statusBadgeClass;

    private String driverName;
    private String driverPhone;
    private String deliveryAddress;

    private int totalQuantity;
    private String itemsSummary;
    private List<OrderItemDetailDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDetailDto {
        private String productName;
        private String sizeName;
        private Integer quantity;
        private BigDecimal subtotal;
        private String formattedSubtotal;
        private String toppingsSummary;
    }
}
