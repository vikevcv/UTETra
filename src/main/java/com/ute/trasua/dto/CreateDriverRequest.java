package com.ute.trasua.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO tiếp nhận dữ liệu cấp tài khoản tài xế mới tại chi nhánh (Module 5 - UC17).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDriverRequest {

    private Long branchId;

    @NotBlank(message = "Họ và tên tài xế không được để trống")
    @Size(min = 2, max = 100, message = "Họ và tên phải từ 2 đến 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email nhận mật khẩu kích hoạt không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại di động Việt Nam không đúng định dạng (10 số)")
    private String phone;

    @NotBlank(message = "Biển số xe không được để trống")
    @Size(min = 6, max = 20, message = "Biển số xe không hợp lệ (VD: 59-X1 888.88)")
    private String licensePlate;

    @Builder.Default
    private String vehicleType = "MOTORBIKE";

    /**
     * Work shift: MORNING, AFTERNOON, EVENING, FULL_TIME
     */
    @NotBlank(message = "Vui lòng chọn ca làm việc phân công")
    private String shift;

    private BigDecimal baseSalary;
}
