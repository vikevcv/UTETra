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
public class ToppingRequest {

    @NotBlank(message = "Tên loại topping không được để trống")
    @Size(min = 2, max = 100, message = "Tên loại topping từ 2 đến 100 ký tự")
    private String toppingName;

    @NotNull(message = "Đơn giá topping không được để trống")
    @DecimalMin(value = "0.0", message = "Đơn giá topping không được âm")
    private BigDecimal price;

    @Builder.Default
    private String status = "ACTIVE";
}
