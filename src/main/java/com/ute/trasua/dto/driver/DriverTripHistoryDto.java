package com.ute.trasua.dto.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO lịch sử các cuốc giao đã hoàn thành hiển thị trên Shipper Drawer (UC15).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverTripHistoryDto {

    private Long orderId;
    private String orderCode;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private Integer cupCount;
    private BigDecimal finalAmount;
    private String status;
    private String pickupPhotoUrl;
    private String deliveryPhotoUrl;
    private LocalDateTime deliveredAt;
    private String deliveredDateFormatted;
    private String deliveredTimeFormatted;
    private String periodCategory;
}
