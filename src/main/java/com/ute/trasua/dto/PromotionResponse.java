package com.ute.trasua.dto;

import com.ute.trasua.entity.Promotion;
import lombok.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data Transfer Object representing promotional voucher details (Module 4 - UC18 & UC24).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionResponse {

    private Long id;
    private String voucherCode;
    private String title;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer totalUsageLimit;
    private Integer usedCount;
    private Integer usageLimitPerUser;
    private String scope;
    private Long branchId;
    private String branchName;
    private String branchCode;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PromotionResponse fromEntity(Promotion p) {
        if (p == null) return null;
        return PromotionResponse.builder()
                .id(p.getId())
                .voucherCode(p.getVoucherCode())
                .title(p.getTitle())
                .description(p.getDescription())
                .discountType(p.getDiscountType())
                .discountValue(p.getDiscountValue())
                .maxDiscountAmount(p.getMaxDiscountAmount())
                .minOrderAmount(p.getMinOrderAmount())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .totalUsageLimit(p.getTotalUsageLimit())
                .usedCount(p.getUsedCount())
                .usageLimitPerUser(p.getUsageLimitPerUser())
                .scope(p.getScope())
                .branchId(p.getBranch() != null ? p.getBranch().getId() : null)
                .branchName(p.getBranch() != null ? p.getBranch().getName() : "")
                .branchCode(p.getBranch() != null ? p.getBranch().getBranchCode() : "")
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public int getRemainingUsage() {
        if (totalUsageLimit == null) return 0;
        int used = (usedCount != null) ? usedCount : 0;
        return Math.max(0, totalUsageLimit - used);
    }

    public int getUsagePercentage() {
        if (totalUsageLimit == null || totalUsageLimit == 0) return 0;
        int used = (usedCount != null) ? usedCount : 0;
        return Math.min(100, (int) Math.round(((double) used / totalUsageLimit) * 100));
    }

    public String getComputedStatus() {
        if (!"ACTIVE".equalsIgnoreCase(this.status)) {
            return "INACTIVE";
        }
        LocalDateTime now = LocalDateTime.now();
        if (startDate != null && now.isBefore(startDate)) {
            return "UPCOMING";
        }
        if (endDate != null && now.isAfter(endDate)) {
            return "EXPIRED";
        }
        if (totalUsageLimit != null && usedCount != null && usedCount >= totalUsageLimit) {
            return "EXHAUSTED";
        }
        return "ACTIVE";
    }

    public String getStatusBadgeClass() {
        return switch (getComputedStatus()) {
            case "ACTIVE" -> "bg-success-subtle text-success border border-success-subtle";
            case "UPCOMING" -> "bg-info-subtle text-info border border-info-subtle";
            case "EXHAUSTED" -> "bg-warning-subtle text-warning border border-warning-subtle";
            case "EXPIRED" -> "bg-secondary-subtle text-secondary border border-secondary-subtle";
            default -> "bg-danger-subtle text-danger border border-danger-subtle";
        };
    }

    public String getStatusText() {
        return switch (getComputedStatus()) {
            case "ACTIVE" -> "Đang áp dụng";
            case "UPCOMING" -> "Sắp diễn ra";
            case "EXHAUSTED" -> "Đã hết lượt";
            case "EXPIRED" -> "Đã hết hạn";
            default -> "Tạm ngưng";
        };
    }

    public String getFormattedDiscount() {
        DecimalFormat df = new DecimalFormat("#,###");
        if ("PERCENT".equalsIgnoreCase(discountType)) {
            String text = "Giảm " + (discountValue != null ? discountValue.stripTrailingZeros().toPlainString() : "0") + "%";
            if (maxDiscountAmount != null && maxDiscountAmount.compareTo(BigDecimal.ZERO) > 0) {
                text += " (Tối đa " + df.format(maxDiscountAmount) + "đ)";
            }
            return text;
        } else {
            return "Giảm " + df.format(discountValue != null ? discountValue : BigDecimal.ZERO) + "đ";
        }
    }

    public String getFormattedMinOrder() {
        if (minOrderAmount == null || minOrderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return "Đơn từ 0đ (Mọi đơn)";
        }
        return "Đơn từ " + new DecimalFormat("#,###").format(minOrderAmount) + "đ";
    }

    public String getFormattedStartDate() {
        if (startDate == null) return "";
        return startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getFormattedEndDate() {
        if (endDate == null) return "";
        return endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getIsoStartDate() {
        if (startDate == null) return "";
        return startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    public String getIsoEndDate() {
        if (endDate == null) return "";
        return endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    public String getDiscountDisplay() {
        return getFormattedDiscount();
    }

    public String getStartDateFormatted() {
        return getFormattedStartDate();
    }

    public String getEndDateFormatted() {
        return getFormattedEndDate();
    }

    public String getComputedStatusLabel() {
        return getStatusText();
    }
}
