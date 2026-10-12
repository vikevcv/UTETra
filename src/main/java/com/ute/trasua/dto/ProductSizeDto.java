package com.ute.trasua.dto;

import com.ute.trasua.entity.ProductSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSizeDto {
    private String sizeCode;
    private String sizeName;
    private BigDecimal additionalPrice;
    private String status;
    private boolean active;

    public static ProductSizeDto fromEntity(ProductSize entity) {
        if (entity == null) return null;
        return ProductSizeDto.builder()
                .sizeCode(entity.getSizeCode())
                .sizeName(entity.getSizeName())
                .additionalPrice(entity.getAdditionalPrice())
                .status(entity.getStatus())
                .active(entity.isActive())
                .build();
    }
}
