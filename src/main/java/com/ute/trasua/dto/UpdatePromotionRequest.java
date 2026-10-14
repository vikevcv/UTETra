package com.ute.trasua.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for updating an existing promotional voucher (Module 4 - UC18 & UC24).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePromotionRequest {

    @NotBlank(message = "Tên chương trình khuyến mãi không được để trống")
    @Size(min = 3, max = 150, message = "Tên chương trình từ 3 đến 150 ký tự")
    private String title;

    private String description;

    @NotBlank(message = "Vui lòng chọn hình thức giảm giá")
    private String discountType;

    @NotNull(message = "Giá trị giảm giá không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", message = "Số tiền giảm tối đa không được âm")
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Giá trị đơn hàng tối thiểu không được để trống")
    @DecimalMin(value = "0.0", message = "Đơn hàng tối thiểu không được âm")
    private BigDecimal minOrderAmount;

    @NotNull(message = "Thời điểm bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @NotNull(message = "Thời điểm kết thúc không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

    @NotNull(message = "Tổng số lượt dùng tối đa không được để trống")
    @Min(value = 1, message = "Tổng số lượt dùng tối thiểu là 1")
    private Integer totalUsageLimit;

    @Min(value = 1, message = "Lượt dùng mỗi khách tối thiểu là 1")
    @Builder.Default
    private Integer usageLimitPerUser = 1;

    @Builder.Default
    private String status = "ACTIVE";
}
