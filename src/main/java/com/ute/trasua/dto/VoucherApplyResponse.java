package com.ute.trasua.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherApplyResponse {

    private boolean valid;
    private String message;
    private String voucherCode;
    private String title;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
}
