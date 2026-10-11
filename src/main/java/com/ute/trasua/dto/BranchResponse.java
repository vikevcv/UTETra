package com.ute.trasua.dto;

import com.ute.trasua.entity.Branch;
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
public class BranchResponse {

    private Long id;
    private String branchCode;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String hotline;
    private String openTime;
    private String closeTime;
    private boolean active;

    private Long managerId;
    private String managerUsername;
    private String managerFullName;
    private String managerPhone;

    private long totalEmployees;
    private long totalActiveEmployees;
    private long totalPosAccounts;

    private LocalDateTime createdAt;

    public static BranchResponse fromEntity(Branch b) {
        if (b == null) return null;
        return fromEntity(b, 0, 0, 0);
    }

    public static BranchResponse fromEntity(Branch b, long totalEmployees, long totalActiveEmployees, long totalPosAccounts) {
        if (b == null) return null;

        Long mgrId = null;
        String mgrUsername = null;
        String mgrFullName = null;
        String mgrPhone = null;

        if (b.getManager() != null) {
            mgrId = b.getManager().getId();
            mgrUsername = b.getManager().getUsername();
            mgrFullName = b.getManager().getFullName();
            mgrPhone = b.getManager().getPhone();
        }

        return BranchResponse.builder()
                .id(b.getId())
                .branchCode(b.getBranchCode())
                .name(b.getName())
                .address(b.getAddress())
                .latitude(b.getLatitude())
                .longitude(b.getLongitude())
                .hotline(b.getHotline())
                .openTime(b.getOpenTime() != null ? b.getOpenTime() : "07:00")
                .closeTime(b.getCloseTime() != null ? b.getCloseTime() : "22:30")
                .active(b.isActive())
                .managerId(mgrId)
                .managerUsername(mgrUsername)
                .managerFullName(mgrFullName != null ? mgrFullName : (mgrUsername != null ? mgrUsername : "Chưa chỉ định"))
                .managerPhone(mgrPhone)
                .totalEmployees(totalEmployees)
                .totalActiveEmployees(totalActiveEmployees)
                .totalPosAccounts(totalPosAccounts)
                .createdAt(b.getCreatedAt())
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
     * Calculate distance from user coordinate to branch using Haversine formula (km)
     */
    public Double calculateDistanceKm(Double userLat, Double userLng) {
        if (latitude == null || longitude == null || userLat == null || userLng == null) {
            return null;
        }
        final int R = 6371; // Earth radius (km)
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
