package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosAccountRequest {

    private Long id;

    private String counterName;

    private String username;

    private String password;

    @Builder.Default
    private boolean active = true;

    private String note;

    private Long branchId;
}
