package com.ute.trasua.dto.handover;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO nhận dữ liệu ảnh đối soát và tọa độ khi tài xế cập nhật trạng thái giao hàng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryActionRequest {
    private String photoData; // Base64 Data URL hoặc HTTP URL ảnh
    private Double latitude;
    private Double longitude;
    private String note;
}
