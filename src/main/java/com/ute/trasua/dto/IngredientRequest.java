package com.ute.trasua.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientRequest {

    @NotBlank(message = "Mã nguyên liệu không được để trống")
    @Size(max = 50, message = "Mã nguyên liệu tối đa 50 ký tự")
    private String code;

    @NotBlank(message = "Tên nguyên liệu không được để trống")
    @Size(max = 150, message = "Tên nguyên liệu tối đa 150 ký tự")
    private String name;

    @NotBlank(message = "Đơn vị tính không được để trống")
    @Size(max = 30, message = "Đơn vị tính tối đa 30 ký tự")
    private String unit;

    @DecimalMin(value = "0.0", message = "Đơn giá tham khảo không được âm")
    @Builder.Default
    private BigDecimal referencePrice = BigDecimal.ZERO;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    @Size(max = 500, message = "URL hình ảnh tối đa 500 ký tự")
    private String imageUrl;

    @Builder.Default
    private String status = "ACTIVE";
}
