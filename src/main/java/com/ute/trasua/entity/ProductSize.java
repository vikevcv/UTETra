package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product cup size entity (Module 5 - UC23).
 * Represents drink sizes (e.g. S, M, L) and their additional pricing across all branches.
 */
@Entity
@Table(name = "product_sizes",
       indexes = {
           @Index(name = "idx_product_size_status", columnList = "status")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSize {

    /**
     * Cup size code ('S', 'M', 'L', 'XL'...). Primary key string.
     */
    @Id
    @Column(name = "size_code", length = 10, nullable = false)
    private String sizeCode;

    /**
     * Display name for the size (e.g. 'Size M - Standard (500ml)').
     */
    @Column(name = "size_name", length = 50, nullable = false)
    private String sizeName;

    /**
     * Additional price for this cup size (VND). Size M: 0, Size L: 6000...
     */
    @Column(name = "additional_price", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal additionalPrice = BigDecimal.ZERO;

    /**
     * Business status: 'ACTIVE' or 'INACTIVE'.
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

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status) || "HoatDong".equalsIgnoreCase(this.status);
    }
}
