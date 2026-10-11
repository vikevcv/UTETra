package com.ute.trasua.dto;

import com.ute.trasua.entity.NhanVien;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVienResponse {

    private Long id;
    private String maNhanVien;
    private String hoTen;
    private String soDienThoai;
    private String cccd;
    private String email;
    private String viTri;
    private String caLamViec;
    private Double mucLuong;
    private String trangThai;
    private LocalDate ngayVaoLam;
    private LocalDateTime createdAt;

    private Long chiNhanhId;
    private String chiNhanhMa;
    private String chiNhanhTen;

    public static NhanVienResponse fromEntity(NhanVien nv) {
        if (nv == null) return null;

        Long cnId = null;
        String cnMa = null;
        String cnTen = null;

        if (nv.getChiNhanh() != null) {
            cnId = nv.getChiNhanh().getId();
            cnMa = nv.getChiNhanh().getMaChiNhanh();
            cnTen = nv.getChiNhanh().getTenChiNhanh();
        }

        return NhanVienResponse.builder()
                .id(nv.getId())
                .maNhanVien(nv.getMaNhanVien())
                .hoTen(nv.getHoTen())
                .soDienThoai(nv.getSoDienThoai())
                .cccd(nv.getCccd())
                .email(nv.getEmail())
                .viTri(nv.getViTri())
                .caLamViec(nv.getCaLamViec())
                .mucLuong(nv.getMucLuong())
                .trangThai(nv.getTrangThai())
                .ngayVaoLam(nv.getNgayVaoLam())
                .createdAt(nv.getCreatedAt())
                .chiNhanhId(cnId)
                .chiNhanhMa(cnMa)
                .chiNhanhTen(cnTen)
                .build();
    }

    public String getViTriTitle() {
        if (viTri == null) return "Chưa xác định";
        return switch (viTri) {
            case "PHA_CHE" -> "Pha Chế";
            case "THU_NGAN" -> "Thu Ngân";
            case "PHUC_VU" -> "Phục Vụ";
            case "QUAN_LY" -> "Quản Lý Ca";
            case "BAO_VE" -> "Bảo Vệ";
            default -> viTri;
        };
    }

    public String getViTriBadgeClass() {
        if (viTri == null) return "zen-badge-secondary";
        return switch (viTri) {
            case "PHA_CHE" -> "zen-badge-primary";
            case "THU_NGAN" -> "zen-badge-info";
            case "PHUC_VU" -> "zen-badge-warning";
            case "QUAN_LY" -> "zen-badge-danger";
            default -> "zen-badge-secondary";
        };
    }

    public String getCaLamViecTitle() {
        if (caLamViec == null) return "Chưa xếp ca";
        return switch (caLamViec) {
            case "CA_SANG" -> "Ca Sáng (06:30 - 12:00)";
            case "CA_CHIEU" -> "Ca Chiều (12:00 - 17:30)";
            case "CA_TOI" -> "Ca Tối (17:30 - 23:00)";
            case "FULL_TIME" -> "Toàn Thời Gian (Full-time)";
            default -> caLamViec;
        };
    }

    public String getStatusTitle() {
        if ("NGHI_VIEC".equals(trangThai)) return "Đã nghỉ việc";
        if ("TAM_NGHI".equals(trangThai)) return "Tạm nghỉ phép";
        return "Đang làm việc";
    }

    public String getStatusBadgeClass() {
        if ("NGHI_VIEC".equals(trangThai)) return "zen-badge-secondary";
        if ("TAM_NGHI".equals(trangThai)) return "zen-badge-warning";
        return "zen-badge-success";
    }

    public String getFormattedLuong() {
        if (mucLuong == null || mucLuong <= 0) return "Chưa thiết lập";
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(mucLuong) + " ₫";
    }

    public String getFormattedNgayVaoLam() {
        if (ngayVaoLam == null) return "Chưa cập nhật";
        return ngayVaoLam.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
