package com.ute.trasua.dto;

import com.ute.trasua.entity.TaiKhoanQuay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaiKhoanQuayResponse {

    private Long id;
    private String tenQuay;
    private String tenDangNhap;
    private boolean active;
    private String ghiChu;
    private LocalDateTime createdAt;

    private Long chiNhanhId;
    private String chiNhanhTen;

    public static TaiKhoanQuayResponse fromEntity(TaiKhoanQuay tk) {
        if (tk == null) return null;

        Long cnId = null;
        String cnTen = null;
        if (tk.getChiNhanh() != null) {
            cnId = tk.getChiNhanh().getId();
            cnTen = tk.getChiNhanh().getTenChiNhanh();
        }

        return TaiKhoanQuayResponse.builder()
                .id(tk.getId())
                .tenQuay(tk.getTenQuay())
                .tenDangNhap(tk.getTenDangNhap())
                .active(tk.isActive())
                .ghiChu(tk.getGhiChu())
                .createdAt(tk.getCreatedAt())
                .chiNhanhId(cnId)
                .chiNhanhTen(cnTen)
                .build();
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "Chưa cập nhật";
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getStatusBadgeClass() {
        return active ? "zen-badge-success" : "zen-badge-secondary";
    }

    public String getStatusTitle() {
        return active ? "Đang hoạt động" : "Tạm khóa";
    }
}
