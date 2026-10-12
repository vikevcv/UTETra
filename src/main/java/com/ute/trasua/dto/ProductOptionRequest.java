package com.ute.trasua.dto;

import com.ute.trasua.entity.ProductOptionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO nhận dữ liệu form thêm mới / cập nhật tùy chọn Size & Topping (UC23).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionRequest {

    private Long id;

    @NotBlank(message = "Mã tùy chọn không được để trống")
    @Size(max = 50, message = "Mã tùy chọn không được vượt quá 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Mã tùy chọn chỉ được chứa chữ in hoa, số và dấu gạch dưới (VD: SIZE_L, TOPPING_TRAN_CHAU)")
    private String optionCode;

    @NotBlank(message = "Tên tùy chọn không được để trống")
    @Size(max = 100, message = "Tên tùy chọn không được vượt quá 100 ký tự")
    private String name;

    @NotNull(message = "Vui lòng chọn phân loại tùy chọn (SIZE hoặc TOPPING)")
    private ProductOptionType type;

    @NotNull(message = "Đơn giá phụ thu không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá phụ thu phải lớn hơn hoặc bằng 0")
    private BigDecimal additionalPrice;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String description;

    @Builder.Default
    private Boolean active = true;
}
