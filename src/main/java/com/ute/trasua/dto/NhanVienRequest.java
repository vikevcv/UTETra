package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVienRequest {

    private Long id;

    private String maNhanVien;

    private String hoTen;

    private String soDienThoai;

    private String cccd;

    private String email;

    @Builder.Default
    private String viTri = "PHA_CHE";

    @Builder.Default
    private String caLamViec = "CA_SANG";

    private Double mucLuong;

    @Builder.Default
    private String trangThai = "DANG_LAM";

    private LocalDate ngayVaoLam;

    private Long chiNhanhId;
}
