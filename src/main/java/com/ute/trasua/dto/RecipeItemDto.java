package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO chứa thông tin một dòng nguyên liệu trong công thức món.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeItemDto {

    private Long id;
    private Long ingredientId;
    private String ingredientCode;
    private String ingredientName;
    private String ingredientUnit;
    private BigDecimal quantity;
    private String note;
}
