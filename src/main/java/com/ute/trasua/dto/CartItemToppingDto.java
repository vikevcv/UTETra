package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing an add-on topping selected for a cup of tea (Module 1 - UC04).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemToppingDto {
    private Long toppingId;
    private String toppingName;
    private BigDecimal price;
}
