package com.ute.trasua.dto.kitchen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenOrderItemDto {
    private Long orderDetailId;
    private Integer cupIndex;
    private Long productId;
    private String productName;
    private String sizeName;
    private Integer quantity;
    private String sugarLevel;
    private String iceLevel;
    @Builder.Default
    private List<String> toppings = new ArrayList<>();
    private String note;
}
