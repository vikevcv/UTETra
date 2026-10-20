package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity định nghĩa công thức pha chế món nước (Định lượng nguyên liệu tiêu chuẩn cho 1 ly).
 */
@Entity
@Table(
    name = "product_recipes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_ingredient", columnNames = {"product_id", "ingredient_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    /**
     * Định lượng tiêu chuẩn cho 1 ly nước (ví dụ: 0.02 Kg trà, 0.10 Lít sữa tươi, 0.03 Lít đường nước...)
     */
    @Column(name = "quantity", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantity;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
