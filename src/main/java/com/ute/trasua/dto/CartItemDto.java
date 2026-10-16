package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing a customized cup of tea in the shopping cart (Module 1 - UC04).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {

    /**
     * Unique hash key identifying the customized cup configuration:
     * productId_sizeCode_sugar_ice_sortedToppingIds_note
     */
    private String itemKey;

    private Long productId;
    private String productName;
    private String productCode;
    private String imageUrl;
    private BigDecimal basePrice;

    private String sizeCode;         // S, M, L
    private String sizeName;         // Chén Nhỏ, Chén Vừa, Ấm Lớn
    @Builder.Default
    private BigDecimal sizeExtraPrice = BigDecimal.ZERO;

    @Builder.Default
    private String sugarLevel = "100%";  // 100%, 70%, 50%, 30%, 0%

    @Builder.Default
    private String iceLevel = "100%";    // 100%, 70%, 50%, 30%, 0%

    @Builder.Default
    private List<CartItemToppingDto> toppings = new ArrayList<>();

    @Builder.Default
    private Integer quantity = 1;

    private String note;

    private BigDecimal unitPrice;    // basePrice + sizeExtraPrice + sum(topping prices)
    private BigDecimal subtotal;     // unitPrice * quantity

    @Builder.Default
    private Boolean available = true;
}
