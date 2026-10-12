package com.ute.trasua.dto.pos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosOrderResponse {

    private Long orderId;
    private String orderCode;
    private Integer dailyQueueNumber;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String vietQrUrl;
    private String status;
    private String paymentStatus;
    private String cashierUsername;
    private LocalDateTime createdAt;
}
