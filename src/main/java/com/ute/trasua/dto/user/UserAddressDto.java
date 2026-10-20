package com.ute.trasua.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddressDto {

    private Long id;
    private String label;
    private String recipientName;
    private String recipientPhone;
    private String addressDetail;
    private Double latitude;
    private Double longitude;
    private boolean isDefault;
    private LocalDateTime createdAt;

    public boolean isDefault() {
        return isDefault;
    }

    public boolean getIsDefault() {
        return isDefault;
    }
}
