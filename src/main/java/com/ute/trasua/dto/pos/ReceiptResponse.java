package com.ute.trasua.dto.pos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptResponse {

    private Long orderId;
    private String orderCode;
    private Integer dailyQueueNumber;
    private String branchName;
    private String branchAddress;
    private String branchHotline;
    private String cashierName;
    private LocalDateTime orderTime;
    private String formattedOrderTime;
    private List<ReceiptItemDto> items;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String paymentMethod;
    private BigDecimal cashTendered;
    private BigDecimal changeAmount;
    private String vietQrQuickUrl;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReceiptItemDto {
        private String productName;
        private String sizeName;
        private String sugarLevel;
        private String iceLevel;
        private List<String> toppings;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String note;
    }
}
