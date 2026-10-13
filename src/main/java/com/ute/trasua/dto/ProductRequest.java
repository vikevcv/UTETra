package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    private Long id;

    private String productCode;

    private String name;

    private Long categoryId;

    private BigDecimal basePrice;

    private String description;

    @Builder.Default
    private String status = "ACTIVE";

    /**
     * File ảnh mới tải lên qua form (sẽ upload lên Cloudinary).
     */
    private MultipartFile imageFile;

    /**
     * URL ảnh hiện tại (nếu người dùng không chọn ảnh mới khi cập nhật).
     */
    private String currentImageUrl;
}
