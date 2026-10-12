package com.ute.trasua.dto;

import com.ute.trasua.entity.Topping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToppingDto {
    private Long id;
    private String toppingName;
    private BigDecimal price;
    private String status;
    private boolean active;

    public static ToppingDto fromEntity(Topping entity) {
        if (entity == null) return null;
        return ToppingDto.builder()
                .id(entity.getId())
                .toppingName(entity.getToppingName())
                .price(entity.getPrice())
                .status(entity.getStatus())
                .active(entity.isActive())
                .build();
    }
}
