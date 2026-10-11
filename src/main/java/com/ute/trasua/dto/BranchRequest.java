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
}
