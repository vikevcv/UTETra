package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO nhận tiêu chí tìm kiếm, lọc và phân trang người dùng trong hệ thống (Module 11 - UC20).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchRequest {

    /** Từ khóa tìm kiếm theo username, email, họ tên hoặc số điện thoại */
    private String keyword;

    /** Lọc theo vai trò: ROLE_CUSTOMER, ROLE_STAFF, ROLE_MANAGER, ROLE_DRIVER, ROLE_ADMIN (null/trống: tất cả) */
    private String role;

    /** Lọc theo trạng thái khóa: true (bị khóa), false (đang hoạt động), null (tất cả) */
    private Boolean locked;

    /** Trang hiện tại (0-indexed) */
    @Builder.Default
    private int page = 0;

    /** Số lượng bản ghi trên một trang */
    @Builder.Default
    private int size = 10;

    /** Cột sắp xếp: id, username, email, fullName, createdAt */
    @Builder.Default
    private String sortBy = "id";

    /** Hướng sắp xếp: asc hoặc desc */
    @Builder.Default
    private String sortDir = "desc";
}
