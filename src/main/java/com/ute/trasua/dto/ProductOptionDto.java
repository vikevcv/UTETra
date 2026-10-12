package com.ute.trasua.dto;

import com.ute.trasua.entity.KichCo;
import com.ute.trasua.entity.ProductOptionType;
import com.ute.trasua.entity.Topping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object tương thích cho ProductOption API (UC23 & UC04).
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

    public static ProductOptionDto fromKichCo(KichCo size) {
        if (size == null) return null;
        return ProductOptionDto.builder()
                .optionCode(size.getMaKichCo())
                .name(size.getTenKichCo())
                .type(ProductOptionType.SIZE)
                .additionalPrice(size.getGiaPhuThu())
                .description("Phụ thu kích cỡ: " + size.getGiaPhuThu() + " đ")
                .active(size.isHoatDong())
                .createdAt(size.getCreatedAt())
                .updatedAt(size.getUpdatedAt())
                .build();
    }

    public static ProductOptionDto fromTopping(Topping topping) {
        if (topping == null) return null;
        return ProductOptionDto.builder()
                .id(topping.getMaTopping())
                .optionCode("TOPPING_" + topping.getMaTopping())
                .name(topping.getTenTopping())
                .type(ProductOptionType.TOPPING)
                .additionalPrice(topping.getDonGia())
                .description("Đơn giá: " + topping.getDonGia() + " đ")
                .active(topping.isHoatDong())
                .createdAt(topping.getCreatedAt())
                .updatedAt(topping.getUpdatedAt())
                .build();
    }
}
