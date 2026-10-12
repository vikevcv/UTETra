package com.ute.trasua.dto;

import com.ute.trasua.entity.Driver;
import com.ute.trasua.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO đại diện cho tài xế giao hàng toàn chuỗi và tại chi nhánh (UC17 & UC25).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverResponse {

    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String phone;
    private String email;
    private boolean locked;
    private String lockReason;
    private LocalDateTime createdAt;

    /** Biển số xe đăng ký */
    private String licensePlate;

    /** Loại xe di chuyển */
    private String vehicleType;

    /** Ca làm việc phân công */
    private String shift;

    /** Trạng thái làm việc: ONLINE (Sẵn sàng), DELIVERING (Đang giao hàng), OFFLINE (Ngoại tuyến) */
    private String workingStatus;

    /** Lương cứng thỏa thuận */
    private BigDecimal baseSalary;

    /** Tổng số đơn hàng đã giao thành công */
    private int totalOrders;

    /** Đánh giá trung bình từ khách hàng (1.0 - 5.0) */
    private double rating;

    /** Tên chi nhánh thường trực */
    private Long branchId;
    private String branchName;
    private String branchCode;

    public static DriverResponse fromEntity(Driver driver) {
        if (driver == null) return null;

        User user = driver.getUser();
        return DriverResponse.builder()
                .id(driver.getId())
                .userId(user != null ? user.getId() : null)
                .username(user != null ? user.getUsername() : "")
                .fullName(user != null ? user.getFullName() : "")
                .phone(user != null ? user.getPhone() : "")
                .email(user != null ? user.getEmail() : "")
                .locked(user != null && user.isLocked())
                .lockReason(user != null ? user.getLockReason() : null)
                .createdAt(driver.getCreatedAt())
                .licensePlate(driver.getLicensePlate())
                .vehicleType(driver.getVehicleType())
                .shift(driver.getShift())
                .workingStatus(driver.getWorkingStatus())
                .baseSalary(driver.getBaseSalary())
                .totalOrders(driver.getCompletedOrders() != null ? driver.getCompletedOrders() : 0)
                .rating(driver.getKpiScore() != null ? driver.getKpiScore() : 5.0)
                .branchId(driver.getBranch() != null ? driver.getBranch().getId() : null)
                .branchName(driver.getBranch() != null ? driver.getBranch().getName() : "")
                .branchCode(driver.getBranch() != null ? driver.getBranch().getBranchCode() : "")
                .build();
    }

    public static DriverResponse fromUser(User user) {
        if (user == null) return null;

        return DriverResponse.builder()
                .id(user.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .locked(user.isLocked())
                .lockReason(user.getLockReason())
                .createdAt(user.getCreatedAt())
                .licensePlate("59-X1 " + String.format("%03d.%02d", (user.getId() * 37) % 999, (user.getId() * 11) % 99))
                .vehicleType("XE_MAY")
                .shift("TOAN_THOI_GIAN")
                .workingStatus(user.isLocked() ? "OFFLINE" : "ONLINE")
                .totalOrders((int) (120 + (user.getId() * 23) % 250))
                .rating(4.9)
                .branchName("UTETra Flagship")
                .build();
    }
}
