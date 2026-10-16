package com.ute.trasua.dto.order;

import com.ute.trasua.dto.CartItemDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Payload for creating an online delivery order (Module 3 - UC05).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {

    @NotBlank(message = "Vui lòng nhập họ và tên người nhận")
    private String customerName;

    @NotBlank(message = "Vui lòng nhập số điện thoại nhận hàng")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không đúng định dạng Việt Nam")
    private String customerPhone;

    @NotBlank(message = "Vui lòng nhập địa chỉ giao hàng chi tiết")
    private String deliveryAddress;

    private Double latitude;
    private Double longitude;

    private Long branchId;
    private String voucherCode;
    private String note;

    @Builder.Default
    private String paymentMethod = "PAYOS";

    @NotEmpty(message = "Giỏ hàng không được để trống")
    @Builder.Default
    private List<CartItemDto> items = new ArrayList<>();
}
