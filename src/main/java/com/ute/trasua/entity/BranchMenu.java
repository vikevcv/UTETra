package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing branch-specific menu item availability and custom pricing (UC22).
 * Quản lý trạng thái còn/hết hàng và giá bán đặc thù của từng sản phẩm tại từng cơ sở chi nhánh.
 */
@Entity
@Table(
    name = "branch_menus",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_branch_product", columnNames = {"branch_id", "product_id"})
    },
    indexes = {
        @Index(name = "idx_branch_menu_branch", columnList = "branch_id"),
        @Index(name = "idx_branch_menu_available", columnList = "available")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * In-stock / available status at this branch:
     * true = Còn hàng / Đang bán
     * false = Hết hàng / Tạm ngưng bán
     */
    @Column(name = "available", nullable = false)
    @Builder.Default
    private boolean available = true;

    /**
     * Optional custom price override for this branch.
     * If null, the chain's default product basePrice is applied.
     */
    @Column(name = "custom_price", precision = 12, scale = 2)
    private BigDecimal customPrice;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Alias methods for compatibility
    public Boolean getIsAvailable() {
        return this.available;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.available = isAvailable != null && isAvailable;
    }
}
