package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaiKhoanQuayRequest {

    private Long id;

    private String tenQuay;

    private String tenDangNhap;

    private String matKhau;

    @Builder.Default
    private boolean active = true;

    private String ghiChu;

    private Long chiNhanhId;
}
