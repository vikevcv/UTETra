package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing branch-specific menu item availability and custom pricing (UC22).
 * Allows branch managers or chain admins to toggle stock status (in-stock / out-of-stock) per branch.
 */
@Entity
@Table(
    name = "branch_menus",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_branch_product", columnNames = {"branch_id", "product_id"})
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
}
