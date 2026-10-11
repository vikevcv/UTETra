package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chi_nhanh",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "ma_chi_nhanh")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiNhanh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_chi_nhanh", nullable = false, length = 30)
    private String maChiNhanh;

    @Column(name = "ten_chi_nhanh", nullable = false, length = 150)
    private String tenChiNhanh;

    @Column(name = "dia_chi", nullable = false, length = 255)
    private String diaChi;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "hotline", length = 20)
    private String hotline;

    @Column(name = "gio_mo_cua", length = 10)
    @Builder.Default
    private String gioMoCua = "07:00";

    @Column(name = "gio_dong_cua", length = 10)
    @Builder.Default
    private String gioDongCua = "22:30";

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Tài khoản người dùng đóng vai trò Quản lý chi nhánh (ROLE_MANAGER).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (gioMoCua == null) {
            gioMoCua = "07:00";
        }
        if (gioDongCua == null) {
            gioDongCua = "22:30";
        }
    }
}
