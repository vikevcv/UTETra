package com.ute.trasua.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ProductSizeRequest {

    @NotBlank(message = "Mã kích cỡ không được để trống (VD: S, M, L)")
    @Size(min = 1, max = 10, message = "Mã kích cỡ từ 1 đến 10 ký tự")
    private String sizeCode;

    @NotBlank(message = "Tên hiển thị kích cỡ không được để trống")
    @Size(min = 2, max = 50, message = "Tên kích cỡ từ 2 đến 50 ký tự")
    private String sizeName;

    @NotNull(message = "Giá phụ thu không được để trống")
    @DecimalMin(value = "0.0", message = "Giá phụ thu không được âm")
    private BigDecimal additionalPrice;

    @Builder.Default
    private String status = "ACTIVE";
}
