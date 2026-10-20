package com.ute.trasua.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddressRequest {

    @NotBlank(message = "Vui lòng chọn hoặc nhập nhãn địa chỉ (VD: Nhà riêng, Công ty...)")
    @Size(max = 50, message = "Nhãn địa chỉ tối đa 50 ký tự")
    private String label;

    @NotBlank(message = "Vui lòng nhập họ và tên người nhận")
    @Size(max = 100, message = "Họ và tên người nhận tối đa 100 ký tự")
    private String recipientName;

    @NotBlank(message = "Vui lòng nhập số điện thoại người nhận")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không đúng định dạng Việt Nam")
    private String recipientPhone;

    @NotBlank(message = "Vui lòng nhập địa chỉ giao hàng chi tiết")
    @Size(max = 255, message = "Địa chỉ chi tiết tối đa 255 ký tự")
    private String addressDetail;

    @NotNull(message = "Vui lòng xác định vị trí trên bản đồ để lưu tọa độ")
    private Double latitude;

    @NotNull(message = "Vui lòng xác định vị trí trên bản đồ để lưu tọa độ")
    private Double longitude;

    private boolean isDefault;
}
