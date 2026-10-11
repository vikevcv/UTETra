package com.ute.trasua.dto;

import com.ute.trasua.entity.ChiNhanh;
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
public class ChiNhanhResponse {

    private Long id;
    private String maChiNhanh;
    private String tenChiNhanh;
    private String diaChi;
    private Double latitude;
    private Double longitude;
    private String hotline;
    private String gioMoCua;
    private String gioDongCua;
    private boolean active;

    private Long managerId;
    private String managerUsername;
    private String managerFullName;
    private String managerPhone;

    private long totalEmployees;
    private long totalActiveEmployees;
    private long totalPosAccounts;

    private LocalDateTime createdAt;

    public static ChiNhanhResponse fromEntity(ChiNhanh c) {
        if (c == null) return null;
        return fromEntity(c, 0, 0, 0);
    }

    public static ChiNhanhResponse fromEntity(ChiNhanh c, long totalEmployees, long totalActiveEmployees, long totalPosAccounts) {
        if (c == null) return null;

        Long mgrId = null;
        String mgrUsername = null;
        String mgrFullName = null;
        String mgrPhone = null;

        if (c.getManager() != null) {
            mgrId = c.getManager().getId();
            mgrUsername = c.getManager().getUsername();
            mgrFullName = c.getManager().getFullName();
            mgrPhone = c.getManager().getPhone();
        }

        return ChiNhanhResponse.builder()
                .id(c.getId())
                .maChiNhanh(c.getMaChiNhanh())
                .tenChiNhanh(c.getTenChiNhanh())
                .diaChi(c.getDiaChi())
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .hotline(c.getHotline())
                .gioMoCua(c.getGioMoCua() != null ? c.getGioMoCua() : "07:00")
                .gioDongCua(c.getGioDongCua() != null ? c.getGioDongCua() : "22:30")
                .active(c.isActive())
                .managerId(mgrId)
                .managerUsername(mgrUsername)
                .managerFullName(mgrFullName != null ? mgrFullName : (mgrUsername != null ? mgrUsername : "Chưa chỉ định"))
                .managerPhone(mgrPhone)
                .totalEmployees(totalEmployees)
                .totalActiveEmployees(totalActiveEmployees)
                .totalPosAccounts(totalPosAccounts)
                .createdAt(c.getCreatedAt())
                .build();
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "Chưa cập nhật";
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getStatusBadgeClass() {
        return active ? "zen-badge-success" : "zen-badge-secondary";
    }

    public String getStatusText() {
        return active ? "Đang hoạt động" : "Tạm dừng";
    }

    /**
     * Tính khoảng cách từ toạ độ người dùng đến chi nhánh theo công thức Haversine (km)
     */
    public Double calculateDistanceKm(Double userLat, Double userLng) {
        if (latitude == null || longitude == null || userLat == null || userLng == null) {
            return null;
        }
        final int R = 6371; // Bán kính Trái Đất (km)
        double latDistance = Math.toRadians(latitude - userLat);
        double lonDistance = Math.toRadians(longitude - userLng);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return Math.round(distance * 100.0) / 100.0;
    }
}
