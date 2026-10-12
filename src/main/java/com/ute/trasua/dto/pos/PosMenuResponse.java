package com.ute.trasua.dto.pos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosMenuResponse {

    private List<CategoryDto> categories;
    private List<ProductDto> products;
    private List<SizeDto> sizes;
    private List<ToppingDto> toppings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryDto {
        private Long id;
        private String name;
        private String description;
        private Integer displayOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductDto {
        private Long id;
        private String productCode;
        private String name;
        private Long categoryId;
        private String categoryName;
        private BigDecimal basePrice;
        private String imageUrl;
        private String description;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SizeDto {
        private Long id;
        private String name;
        private Integer volumeMl;
        private BigDecimal extraPrice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ToppingDto {
        private Long id;
        private String name;
        private BigDecimal price;
        private String status;
    }
}
