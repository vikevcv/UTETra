package com.ute.trasua.dto;

import com.ute.trasua.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO an toàn trả về thông tin người dùng cho giao diện quản trị (Module 11 - UC20, UC25).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String avatar;
    private boolean enabled;
    private boolean locked;
    private String lockReason;
    private LocalDateTime createdAt;

    private Long branchId;
    private String branchName;
    private String branchCode;

    @Builder.Default
    private Set<String> roles = new HashSet<>();

    public static UserResponse fromEntity(User user) {
        if (user == null) return null;

        Long bId = null;
        String bName = null;
        String bCode = null;
        if (user.getBranch() != null) {
            bId = user.getBranch().getId();
            bName = user.getBranch().getName();
            bCode = user.getBranch().getBranchCode();
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName() != null ? user.getFullName() : user.getUsername())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .enabled(user.isEnabled())
                .locked(user.isLocked())
                .lockReason(user.getLockReason())
                .branchId(bId)
                .branchName(bName)
                .branchCode(bCode)
                .roles(user.getRoles() != null ? new HashSet<>(user.getRoles()) : new HashSet<>())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "Chưa cập nhật";
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getPrimaryRole() {
        if (roles == null || roles.isEmpty()) return "CUSTOMER";
        if (roles.contains("ROLE_ADMIN")) return "ADMIN";
        if (roles.contains("ROLE_MANAGER")) return "MANAGER";
        if (roles.contains("ROLE_STAFF")) return "STAFF";
        if (roles.contains("ROLE_DRIVER")) return "DRIVER";
        return "CUSTOMER";
    }

    public String getRoleBadgeClass() {
        String role = getPrimaryRole();
        return switch (role) {
            case "ADMIN" -> "zen-badge-danger";
            case "MANAGER" -> "zen-badge-warning";
            case "STAFF" -> "zen-badge-info";
            case "DRIVER" -> "zen-badge-primary";
            default -> "zen-badge-secondary";
        };
    }
}
