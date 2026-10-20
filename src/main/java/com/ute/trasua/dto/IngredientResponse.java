package com.ute.trasua.dto;

import com.ute.trasua.entity.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientResponse {

    private Long id;
    private String code;
    private String name;
    private String unit;
    private BigDecimal referencePrice;
    private String formattedPrice;
    private String description;
    private String imageUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static IngredientResponse fromEntity(Ingredient i) {
        if (i == null) return null;

        String formatted = "0 đ";
        if (i.getReferencePrice() != null) {
            DecimalFormat df = new DecimalFormat("#,### đ");
            formatted = df.format(i.getReferencePrice());
        }

        return IngredientResponse.builder()
                .id(i.getId())
                .code(i.getCode())
                .name(i.getName())
                .unit(i.getUnit())
                .referencePrice(i.getReferencePrice())
                .formattedPrice(formatted)
                .description(i.getDescription())
                .imageUrl(i.getImageUrl())
                .status(i.getStatus())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }
}
