package com.ute.trasua.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportReceiptItemRequest {

    @NotNull(message = "Vui lòng chọn nguyên liệu nhập")
    private Long ingredientId;

    @NotNull(message = "Số lượng nhập không được để trống")
    @DecimalMin(value = "0.01", message = "Số lượng nhập phải lớn hơn 0")
    private BigDecimal quantity;

    @NotNull(message = "Đơn giá nhập không được để trống")
    @DecimalMin(value = "0.0", message = "Đơn giá nhập không được âm")
    private BigDecimal unitPrice;

    private LocalDate expiryDate;

    @Size(max = 255, message = "Ghi chú dòng tối đa 255 ký tự")
    private String note;
}
