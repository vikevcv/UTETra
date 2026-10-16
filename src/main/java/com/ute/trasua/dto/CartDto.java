package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing the customer's complete shopping cart (Module 1 - UC04).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {

    @Builder.Default
    private List<CartItemDto> items = new ArrayList<>();

    @Builder.Default
    private int totalQuantity = 0;

    @Builder.Default
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private String appliedVoucherCode;

    @Builder.Default
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}
