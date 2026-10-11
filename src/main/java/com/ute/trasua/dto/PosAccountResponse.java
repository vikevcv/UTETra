package com.ute.trasua.dto;

import com.ute.trasua.entity.PosAccount;
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
public class PosAccountResponse {

    private Long id;
    private String counterName;
    private String username;
    private boolean active;
    private String note;
    private LocalDateTime createdAt;

    private Long branchId;
    private String branchName;

    public static PosAccountResponse fromEntity(PosAccount pos) {
        if (pos == null) return null;

        Long bId = null;
        String bName = null;
        if (pos.getBranch() != null) {
            bId = pos.getBranch().getId();
            bName = pos.getBranch().getName();
        }

        return PosAccountResponse.builder()
                .id(pos.getId())
                .counterName(pos.getCounterName())
                .username(pos.getUsername())
                .active(pos.isActive())
                .note(pos.getNote())
                .createdAt(pos.getCreatedAt())
                .branchId(bId)
                .branchName(bName)
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
