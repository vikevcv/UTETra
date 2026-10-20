package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing raw ingredients and packaging materials (Task 4.1 / Module 14).
 * Table: ingredients (Reference Table 3.18 in report).
 */
@Entity
@Table(
    name = "ingredients",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_ingredient_code", columnNames = {"code"}),
        @UniqueConstraint(name = "uk_ingredient_name", columnNames = {"name"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /**
     * Unit of measurement: Kg, Liter, Box, Bag, Bottle, Pack, Roll...
     */
    @Column(name = "unit", nullable = false, length = 30)
    private String unit;

    /**
     * Standard reference purchase price (VNĐ).
     */
    @Column(name = "reference_price", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal referencePrice = BigDecimal.ZERO;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Status: 'ACTIVE', 'INACTIVE'
     */
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "ACTIVE";
        }
        if (this.referencePrice == null) {
            this.referencePrice = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
