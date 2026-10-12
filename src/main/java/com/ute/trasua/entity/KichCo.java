package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thực thể Kích cỡ ly đồ uống (Module 5 - UC23).
 * Ánh xạ bảng KichCo (kich_co) theo chuẩn sơ đồ ERD Báo Cáo SE.
 */
@Entity
@Table(name = "kich_co",
       indexes = {
           @Index(name = "idx_kich_co_trang_thai", columnList = "trang_thai")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KichCo {

    /**
     * Mã kích cỡ ly ('S', 'M', 'L', 'XL'...). Khóa chính chuẩn hóa chuỗi.
     * Cột MaKichCo trong ERD.
     */
    @Id
    @Column(name = "ma_kich_co", length = 10, nullable = false)
    private String maKichCo;

    /**
     * Tên hiển thị kích cỡ (VD: 'Size M - Cỡ Tiêu Chuẩn (500ml)').
     * Cột TenKichCo trong ERD.
     */
    @Column(name = "ten_kich_co", length = 50, nullable = false)
    private String tenKichCo;

    /**
     * Giá cộng thêm theo kích cỡ ly (VNĐ). Size M: 0đ; Size L: 6.000đ...
     * Cột GiaPhuThu trong ERD / Báo Cáo SE.
     */
    @Column(name = "gia_phu_thu", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal giaPhuThu = BigDecimal.ZERO;

    /**
     * Trạng thái kinh doanh: 'HoatDong' (Đang bán) hoặc 'TamNgung' (Ngừng bán).
     * Cột TrangThai phục vụ UC23.
     */
    @Column(name = "trang_thai", length = 20, nullable = false)
    @Builder.Default
    private String trangThai = "HoatDong";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isHoatDong() {
        return "HoatDong".equalsIgnoreCase(this.trangThai);
    }
}
