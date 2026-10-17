package com.ute.trasua.dto.handover;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO nhận thông tin báo cáo giao hàng thất bại từ tài xế (UC14).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryFailureRequest {

    @NotBlank(message = "Lý do giao hàng thất bại không được để trống")
    private String reasonCode; // KHONG_LIEN_LAC_DUOC, KHACH_TU_CHOI, SAI_DIA_CHI

    private String proofPhotoData; // Base64 data URL ảnh chụp màn hình cuộc gọi hoặc hiện trường

    private String note;

    @Builder.Default
    private Integer callAttempts = 3;
}
