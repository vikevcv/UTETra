package com.ute.trasua.dto.pos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosPaymentRequest {

    private Long orderId;
    
    /**
     * CASH, VIETQR_BANK_TRANSFER
     */
    private String paymentMethod;
    
    private BigDecimal amountPaid;
    private BigDecimal cashTendered;
    private BigDecimal changeAmount;
    private String transactionCode;
}
