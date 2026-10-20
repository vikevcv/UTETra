package com.ute.trasua.dto;

import com.ute.trasua.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private Integer displayOrder;
    private boolean active;
    private long totalProducts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CategoryResponse fromEntity(Category c) {
        return fromEntity(c, 0);
    }

    public static CategoryResponse fromEntity(Category c, long totalProducts) {
        if (c == null) return null;
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .displayOrder(c.getDisplayOrder())
                .active(c.isActive())
                .totalProducts(totalProducts)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
