package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thực thể Danh mục Topping thêm (Module 5 - UC23).
 * Ánh xạ bảng Topping (topping) theo chuẩn sơ đồ ERD Báo Cáo SE.
 */
@Entity
@Table(name = "topping",
       indexes = {
           @Index(name = "idx_topping_trang_thai", columnList = "trang_thai")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topping {

    /**
     * Mã định danh loại topping. Khóa chính tự tăng (Identity).
     * Cột MaTopping trong ERD.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_topping")
    private Long maTopping;

    /**
     * Tên loại topping thêm (VD: 'Trân Châu Hoàng Kim', 'Kem Phô Mai Cheese Foam').
     * Cột TenTopping trong ERD.
     */
    @Column(name = "ten_topping", length = 100, nullable = false, unique = true)
    private String tenTopping;

    /**
     * Đơn giá một phần topping (VNĐ) áp dụng toàn chuỗi.
     * Cột DonGia trong ERD / Báo Cáo SE.
     */
    @Column(name = "don_gia", precision = 18, scale = 2, nullable = false)
    private BigDecimal donGia;

    /**
     * Trạng thái kinh doanh: 'HoatDong' (Đang bán) hoặc 'TamNgung' (Ngừng bán).
     * Cột TrangThai trong ERD / Báo Cáo SE.
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
