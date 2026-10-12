package com.ute.trasua.dto;

import com.ute.trasua.entity.ProductOptionType;
import com.ute.trasua.entity.ProductSize;
import com.ute.trasua.entity.Topping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for ProductOption API (UC23 & UC04).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    public static ProductOptionDto fromProductSize(ProductSize size) {
        if (size == null) return null;
        return ProductOptionDto.builder()
                .optionCode(size.getSizeCode())
                .name(size.getSizeName())
                .type(ProductOptionType.SIZE)
                .additionalPrice(size.getAdditionalPrice())
                .description("Phụ thu kích cỡ: " + size.getAdditionalPrice() + " đ")
                .active(size.isActive())
                .createdAt(size.getCreatedAt())
                .updatedAt(size.getUpdatedAt())
                .build();
    }

    public static ProductOptionDto fromTopping(Topping topping) {
        if (topping == null) return null;
        return ProductOptionDto.builder()
                .id(topping.getId())
                .optionCode("TOPPING_" + topping.getId())
                .name(topping.getToppingName())
                .type(ProductOptionType.TOPPING)
                .additionalPrice(topping.getPrice())
                .description("Đơn giá: " + topping.getPrice() + " đ")
                .active(topping.isActive())
                .createdAt(topping.getCreatedAt())
                .updatedAt(topping.getUpdatedAt())
                .build();
    }
}
