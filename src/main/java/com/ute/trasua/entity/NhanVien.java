package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "nhan_vien",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "ma_nhan_vien")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_nhan_vien", nullable = false, length = 30)
    private String maNhanVien;

    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "cccd", length = 20)
    private String cccd;

    @Column(name = "email", length = 100)
    private String email;

    /**
     * Vị trí công việc: PHA_CHE, THU_NGAN, PHUC_VU, QUAN_LY, BAO_VE
     */
    @Column(name = "vi_tri", length = 50)
    @Builder.Default
    private String viTri = "PHA_CHE";

    /**
     * Ca làm việc: CA_SANG (06:30 - 12:00), CA_CHIEU (12:00 - 17:30), CA_TOI (17:30 - 23:00), FULL_TIME
     */
    @Column(name = "ca_lam_viec", length = 50)
    @Builder.Default
    private String caLamViec = "CA_SANG";

    @Column(name = "muc_luong")
    private Double mucLuong;

    /**
     * Trạng thái nhân sự: DANG_LAM, NGHI_VIEC, TAM_NGHI
     */
    @Column(name = "trang_thai", length = 30)
    @Builder.Default
    private String trangThai = "DANG_LAM";

    @Column(name = "ngay_vao_lam")
    private LocalDate ngayVaoLam;

    /**
     * Chi nhánh trực thuộc
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chi_nhanh_id", nullable = false)
    private ChiNhanh chiNhanh;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (ngayVaoLam == null) {
            ngayVaoLam = LocalDate.now();
        }
        if (trangThai == null) {
            trangThai = "DANG_LAM";
        }
        if (viTri == null) {
            viTri = "PHA_CHE";
        }
        if (caLamViec == null) {
            caLamViec = "CA_SANG";
        }
    }
}
