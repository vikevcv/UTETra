package com.ute.trasua.dto.kitchen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchMenuStatusDto {
    private Long id; // product id or topping id
    private String itemType; // "PRODUCT" hoặc "TOPPING"
    private String code;
    private String name;
    private String categoryName;
    private BigDecimal price;
    private String imageUrl;
    @com.fasterxml.jackson.annotation.JsonProperty("isAvailable")
    private Boolean isAvailable;
    private Integer stockEstimate; // Ước lượng tồn kho phục vụ hiển thị game-like
}
