package com.ute.trasua.dto.kitchen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToggleMenuRequest {
    private Long branchId;
    private Long itemId; // productId hoặc toppingId
    private String itemType; // "PRODUCT" hoặc "TOPPING"
    private Boolean isAvailable;
}
