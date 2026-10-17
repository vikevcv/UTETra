package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchRequest {

    private Long id;

    private String branchCode;

    private String name;

    private String address;

    private Double latitude;

    private Double longitude;

    private String hotline;

    @Builder.Default
    private String openTime = "07:00";

    @Builder.Default
    private String closeTime = "22:30";

    @Builder.Default
    private boolean active = true;

    private Long managerId;
    
    // Feedback 1 & 2: Quản lý chi nhánh mới & Tự động cấp tài khoản
    @Builder.Default
    private String managerOption = "NEW"; // "NEW" (Tự nhập & cấp tài khoản) hoặc "EXISTING" (Chọn quản lý có sẵn)

    private String newManagerFullName;

    private String newManagerPhone;

    private String newManagerEmail;

    private String newManagerUsername; // Tùy chọn (để trống sẽ tự sinh ql_<branchCode>)

    private String newManagerPassword; // Tùy chọn (để trống sẽ dùng mặc định an toàn Branch@123)
}
