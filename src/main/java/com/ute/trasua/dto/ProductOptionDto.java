package com.ute.trasua.dto;

import com.ute.trasua.entity.ProductOption;
import com.ute.trasua.entity.ProductOptionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object cho ProductOption (tránh để lộ Entity ra View/API).
 */
@Data
@Builder
public class ProductOptionDto {
    private Long id;
    private String optionCode;
    private String name;
    private ProductOptionType type;
    private BigDecimal additionalPrice;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductOptionDto fromEntity(ProductOption entity) {
        if (entity == null) {
            return null;
        }
        return ProductOptionDto.builder()
                .id(entity.getId())
                .optionCode(entity.getOptionCode())
                .name(entity.getName())
                .type(entity.getType())
                .additionalPrice(entity.getAdditionalPrice())
                .description(entity.getDescription())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
