package com.ute.trasua.dto;

import com.ute.trasua.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO đại diện cho tài xế giao hàng toàn chuỗi phục vụ giám sát và điều phối (Module 11 - UC25).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverResponse {

    private Long id;
    private String username;
    private String fullName;
    private String phone;
    private String email;
    private boolean locked;
    private String lockReason;
    private LocalDateTime createdAt;

    /** Biển số xe đăng ký */
    private String licensePlate;

    /** Trạng thái làm việc: ONLINE (Sẵn sàng), DELIVERING (Đang giao hàng), OFFLINE (Ngoại tuyến) */
    private String workingStatus;

    /** Tổng số đơn hàng đã giao thành công */
    private int totalOrders;

    /** Đánh giá trung bình từ khách hàng (1.0 - 5.0) */
    private double rating;

    /** Khu vực hoặc cơ sở thường trực */
    private String operatingArea;

    public static DriverResponse fromUser(User user) {
        if (user == null) return null;

        // Sinh dữ liệu mô phỏng trực quan dựa trên ID của tài xế
        long seed = user.getId() != null ? user.getId() : 1L;
        String[] mockPlates = {"59X1-888.88", "59S2-345.67", "50T1-999.12", "60B3-123.45", "61C1-567.89"};
        String[] mockAreas = {"TP. Thủ Đức (CN1)", "Quận 1 (CN2)", "Bình Thạnh (CN3)", "Gò Vấp (CN4)"};
        String[] mockStatuses = {"ONLINE", "DELIVERING", "ONLINE", "OFFLINE"};

        int plateIdx = (int) (seed % mockPlates.length);
        int areaIdx = (int) (seed % mockAreas.length);
        int statusIdx = user.isLocked() ? 3 : (int) (seed % mockStatuses.length);

        int orders = (int) (120 + (seed * 37) % 350);
        double rate = Math.round((4.6 + ((seed * 7) % 5) * 0.08) * 10.0) / 10.0;
        if (rate > 5.0) rate = 5.0;

        return DriverResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName() != null ? user.getFullName() : user.getUsername())
                .phone(user.getPhone() != null ? user.getPhone() : "Chưa cập nhật SĐT")
                .email(user.getEmail())
                .locked(user.isLocked())
                .lockReason(user.getLockReason())
                .createdAt(user.getCreatedAt())
                .licensePlate(mockPlates[plateIdx])
                .workingStatus(user.isLocked() ? "SUSPENDED" : mockStatuses[statusIdx])
                .totalOrders(orders)
                .rating(rate)
                .operatingArea(mockAreas[areaIdx])
                .build();
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "Chưa cập nhật";
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getStatusBadgeClass() {
        return switch (workingStatus) {
            case "ONLINE" -> "zen-badge-success";
            case "DELIVERING" -> "zen-badge-warning";
            case "OFFLINE" -> "zen-badge-secondary";
            case "SUSPENDED" -> "zen-badge-danger";
            default -> "zen-badge-secondary";
        };
    }

    public String getStatusText() {
        return switch (workingStatus) {
            case "ONLINE" -> "Đang trực tuyến";
            case "DELIVERING" -> "Đang giao hàng";
            case "OFFLINE" -> "Ngoại tuyến";
            case "SUSPENDED" -> "Đã tạm khóa";
            default -> "Không xác định";
        };
    }
}
