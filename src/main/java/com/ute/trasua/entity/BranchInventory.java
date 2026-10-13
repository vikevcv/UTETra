package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing stock quantity of an ingredient at a specific branch (Task 4.1 / Module 14).
 * Table: branch_inventories (Reference Table 3.21 in report).
 */
@Entity
@Table(
    name = "branch_inventories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_branch_ingredient", columnNames = {"branch_id", "ingredient_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    /**
     * Current available stock quantity.
     * Auto-incremented on import receipt, auto-decremented on drink preparation/order completion.
     */
    @Column(name = "stock_quantity", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal stockQuantity = BigDecimal.ZERO;

    /**
     * Minimum stock threshold for low-stock warning alert. Default: 10.0.
     */
    @Column(name = "min_threshold", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minThreshold = new BigDecimal("10.00");

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
        if (this.stockQuantity == null) {
            this.stockQuantity = BigDecimal.ZERO;
        }
        if (this.minThreshold == null) {
            this.minThreshold = new BigDecimal("10.00");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if current stock is at or below the warning threshold.
     */
    public boolean isLowStock() {
        return stockQuantity != null && minThreshold != null && stockQuantity.compareTo(minThreshold) <= 0;
    }

    /**
     * Check if current stock is empty or negative (<= 0).
     */
    public boolean isOutOfStock() {
        return stockQuantity == null || stockQuantity.compareTo(BigDecimal.ZERO) <= 0;
    }
}
