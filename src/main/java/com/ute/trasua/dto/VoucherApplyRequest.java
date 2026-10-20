package com.ute.trasua.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherApplyRequest {

    @NotBlank(message = "Vui lòng nhập mã voucher")
    private String voucherCode;

    private Long branchId;

    @NotNull(message = "Số tiền đơn hàng không được để trống")
    private BigDecimal orderSubtotal;
}
