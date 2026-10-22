package com.ute.trasua.dto;

import com.ute.trasua.entity.Product;
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
public class ProductResponse {

    private Long id;
    private String productCode;
    private String name;
    private Long categoryId;
    private String categoryName;
    private BigDecimal basePrice;
    private String formattedPrice;
    private String imageUrl;
    private String description;
    private String status;
    private Double averageRating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private java.util.List<RecipeItemDto> recipes;

    @Builder.Default
    private Boolean available = true;
    private Long branchId;

    public static ProductResponse fromEntity(Product p) {
        if (p == null) return null;

        Long catId = null;
        String catName = null;
        if (p.getCategory() != null) {
            catId = p.getCategory().getId();
            catName = p.getCategory().getName();
        }

        String formatted = "0 đ";
        if (p.getBasePrice() != null) {
            DecimalFormat df = new DecimalFormat("#,### đ");
            formatted = df.format(p.getBasePrice());
        }

        return ProductResponse.builder()
                .id(p.getId())
                .productCode(p.getProductCode())
                .name(p.getName())
                .categoryId(catId)
                .categoryName(catName)
                .basePrice(p.getBasePrice())
                .formattedPrice(formatted)
                .imageUrl(p.getImageUrl())
                .description(p.getDescription())
                .status(p.getStatus())
                .averageRating(p.getAverageRating() != null ? p.getAverageRating() : 5.0)
                .reviewCount(p.getReviewCount() != null ? p.getReviewCount() : 0)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
