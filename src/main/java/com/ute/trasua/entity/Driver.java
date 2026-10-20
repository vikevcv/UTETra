package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thực thể Tài xế giao hàng (Module 5 - UC17).
 * Ánh xạ chuẩn theo sơ đồ ERD Báo Cáo SE: bảng TaiXe (drivers).
 */
@Entity
@Table(name = "drivers",
       indexes = {
           @Index(name = "idx_driver_user_id", columnList = "user_id", unique = true),
           @Index(name = "idx_driver_branch_id", columnList = "branch_id"),
           @Index(name = "idx_driver_working_status", columnList = "working_status")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tài khoản người dùng đăng nhập hệ thống (ROLE_DRIVER).
     * Bảng NguoiDung trong ERD.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Chi nhánh phân công phụ trách giao hàng.
     * Bảng ChiNhanh trong ERD (MaChiNhanh).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    /**
     * Biển số xe đăng ký (BienSoXe trong ERD).
     */
    @Column(name = "license_plate", nullable = false, length = 30)
    private String licensePlate;

    /**
     * Vehicle type: MOTORBIKE, ELECTRIC_BIKE...
     */
    @Column(name = "vehicle_type", length = 30)
    @Builder.Default
    private String vehicleType = "MOTORBIKE";

    /**
     * Trạng thái hoạt động (TrangThaiHoatDong trong ERD):
     * ONLINE (Sẵn sàng nhận đơn), DELIVERING (Đang giao hàng), OFFLINE (Ngoại tuyến/Nghỉ ca).
     */
    @Column(name = "working_status", length = 30)
    @Builder.Default
    private String workingStatus = "ONLINE";

    /**
     * Lương cứng thỏa thuận (LuongCung trong ERD).
     */
    @Column(name = "base_salary", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal baseSalary = BigDecimal.ZERO;

    /**
     * Điểm đánh giá KPI trung bình (DiemKPI trong ERD).
     */
    @Column(name = "kpi_score")
    @Builder.Default
    private Double kpiScore = 5.0;

    /**
     * Tổng số đơn đã giao thành công (SoDonGiaoThanhCong trong ERD).
     */
    @Column(name = "completed_orders")
    @Builder.Default
    private Integer completedOrders = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(name = "version")
    private Long version;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
