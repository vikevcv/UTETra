package com.ute.trasua.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerificationRequest {

    private String email; // Sẽ được bind tự động từ flash attribute hoặc hidden input

    @NotBlank(message = "Mã OTP không được để trống")
    private String otp;
}
