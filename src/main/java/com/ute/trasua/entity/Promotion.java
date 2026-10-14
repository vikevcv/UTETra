package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Promotion & Voucher Entity (Module 4 - UC18 & UC24).
 * Maps to table 'promotions' according to the Software Engineering project ERD (Table 3.12: KhuyenMai).
 */
@Entity
@Table(name = "promotions",
       indexes = {
           @Index(name = "idx_promotion_voucher_code", columnList = "voucher_code", unique = true),
           @Index(name = "idx_promotion_status", columnList = "status"),
           @Index(name = "idx_promotion_scope", columnList = "scope"),
           @Index(name = "idx_promotion_branch_id", columnList = "branch_id"),
           @Index(name = "idx_promotion_dates", columnList = "start_date, end_date")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique promotional voucher code (e.g. 'CHAOMUNG2026', 'THUDUC15K').
     * Column 'MaCode' in ERD.
     */
    @Column(name = "voucher_code", length = 50, nullable = false, unique = true)
    private String voucherCode;

    /**
     * Display title of the campaign.
     * Column 'TenKhuyenMai' in ERD.
     */
    @Column(name = "title", length = 150, nullable = false)
    private String title;

    /**
     * Detailed description of terms and conditions.
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Discount method: 'PERCENT' (percentage %) or 'FIXED_AMOUNT' (fixed amount in VND).
     * Column 'LoaiGiamGia' in ERD.
     */
    @Column(name = "discount_type", length = 20, nullable = false)
    @Builder.Default
    private String discountType = "PERCENT";

    /**
     * Discount value: percentage (e.g. 20 for 20%) or fixed amount (e.g. 15000 for 15,000 VND).
     * Column 'GiaTriGiam' in ERD.
     */
    @Column(name = "discount_value", precision = 18, scale = 2, nullable = false)
    private BigDecimal discountValue;

    /**
     * Maximum discount ceiling for percentage-based vouchers. Nullable for fixed-amount vouchers.
     * Column 'GiamToiDa' in ERD.
     */
    @Column(name = "max_discount_amount", precision = 18, scale = 2)
    private BigDecimal maxDiscountAmount;

    /**
     * Minimum order subtotal required to apply this voucher.
     * Column 'DonHangToiThieu' in ERD.
     */
    @Column(name = "min_order_amount", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    /**
     * Validity start timestamp.
     * Column 'NgayBatDau' in ERD.
     */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * Validity expiration timestamp.
     * Column 'NgayKetThuc' in ERD.
     */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * Total number of usages allocated for this voucher.
     * Column 'SoLuongPhatHanh' in ERD.
     */
    @Column(name = "total_usage_limit", nullable = false)
    @Builder.Default
    private Integer totalUsageLimit = 100;

    /**
     * Number of times this voucher has been used.
     * Column 'SoLuongDaDung' in ERD.
     */
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    /**
     * Usage limit per customer account (default: 1).
     */
    @Column(name = "usage_limit_per_user")
    @Builder.Default
    private Integer usageLimitPerUser = 1;

    /**
     * Application scope: 'GLOBAL' (chain-wide) or 'BRANCH' (store-specific).
     * Column 'PhamViApDung' in ERD.
     */
    @Column(name = "scope", length = 20, nullable = false)
    @Builder.Default
    private String scope = "GLOBAL";

    /**
     * Branch assigned when scope is 'BRANCH'. Null when scope is 'GLOBAL'.
     * Column 'MaChiNhanh' in ERD.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    /**
     * Status: 'ACTIVE' (enabled) or 'INACTIVE' (suspended/disabled).
     * Column 'TrangThai' in ERD.
     */
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Business validation whether this voucher is currently active and usable at this exact moment.
     */
    public boolean isCurrentlyActive() {
        if (!"ACTIVE".equalsIgnoreCase(this.status)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(this.startDate) || now.isAfter(this.endDate)) {
            return false;
        }
        return this.usedCount < this.totalUsageLimit;
    }

    /**
     * Computes real-time business status.
     * Values: 'ACTIVE', 'UPCOMING', 'EXPIRED', 'EXHAUSTED', 'INACTIVE'.
     */
    public String getComputedStatus() {
        if (!"ACTIVE".equalsIgnoreCase(this.status)) {
            return "INACTIVE";
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(this.startDate)) {
            return "UPCOMING";
        }
        if (now.isAfter(this.endDate)) {
            return "EXPIRED";
        }
        if (this.usedCount >= this.totalUsageLimit) {
            return "EXHAUSTED";
        }
        return "ACTIVE";
    }

    /**
     * Calculate discount amount for an order subtotal.
     */
    public BigDecimal calculateDiscount(BigDecimal orderSubtotal) {
        if (orderSubtotal == null || orderSubtotal.compareTo(this.minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;
        if ("PERCENT".equalsIgnoreCase(this.discountType)) {
            discount = orderSubtotal.multiply(this.discountValue)
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            if (this.maxDiscountAmount != null && this.maxDiscountAmount.compareTo(BigDecimal.ZERO) > 0) {
                discount = discount.min(this.maxDiscountAmount);
            }
        } else if ("FIXED_AMOUNT".equalsIgnoreCase(this.discountType)) {
            discount = this.discountValue;
        }

        return discount.min(orderSubtotal);
    }
}
