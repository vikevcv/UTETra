package com.ute.trasua.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for creating a new promotional voucher (Module 4 - UC18 & UC24).
 * Conforms to business form QLKM_BM1.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePromotionRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    @Size(min = 3, max = 50, message = "Mã voucher từ 3 đến 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Mã voucher chỉ chứa chữ cái, số và dấu gạch dưới")
    private String voucherCode;

    @NotBlank(message = "Tên chương trình khuyến mãi không được để trống")
    @Size(min = 3, max = 150, message = "Tên chương trình từ 3 đến 150 ký tự")
    private String title;

    private String description;

    @NotBlank(message = "Vui lòng chọn hình thức giảm giá")
    @Builder.Default
    private String discountType = "PERCENT";

    @NotNull(message = "Giá trị giảm giá không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", message = "Số tiền giảm tối đa không được âm")
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Giá trị đơn hàng tối thiểu không được để trống")
    @DecimalMin(value = "0.0", message = "Đơn hàng tối thiểu không được âm")
    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @NotNull(message = "Thời điểm bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @NotNull(message = "Thời điểm kết thúc không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

    @NotNull(message = "Tổng số lượt dùng tối đa không được để trống")
    @Min(value = 1, message = "Tổng số lượt dùng tối thiểu là 1")
    @Builder.Default
    private Integer totalUsageLimit = 100;

    @Min(value = 1, message = "Lượt dùng mỗi khách tối thiểu là 1")
    @Builder.Default
    private Integer usageLimitPerUser = 1;

    @Builder.Default
    private String scope = "GLOBAL";

    private Long branchId;
}
