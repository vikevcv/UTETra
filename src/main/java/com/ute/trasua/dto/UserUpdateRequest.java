package com.ute.trasua.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO nhận yêu cầu cập nhật tài khoản người dùng từ Admin (Module 11 - UC20).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @Size(max = 100, message = "Họ và tên không vượt quá 100 ký tự")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại không hợp lệ")
    private String phone;

    private String avatar;

    private Boolean locked;

    private String lockReason;

    private Set<String> roles;

    /**
     * Chi nhánh phân công trực thuộc (Bắt buộc đối với ROLE_MANAGER, ROLE_STAFF, ROLE_DRIVER).
     */
    private Long branchId;
}
