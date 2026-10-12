package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Drink topping add-on entity (Module 5 - UC23 & Module POS).
 * Represents drink toppings (e.g. Pearls, Jelly, Pudding) and their unit pricing.
 */
@Entity
@Table(name = "toppings",
       indexes = {
           @Index(name = "idx_topping_status", columnList = "status")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topping {

    /**
     * Unique identifier for topping. Auto-increment identity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Display name of the topping (e.g. 'Golden Pearl', 'Cheese Foam').
     */
    @Column(name = "topping_name", length = 100, nullable = false, unique = true)
    private String toppingName;

    /**
     * Unit price for this topping (VND).
     */
    @Column(name = "price", precision = 18, scale = 2, nullable = false)
    private BigDecimal price;

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

    // Compatibility getter and setter for POS / legacy references
    public String getName() {
        return this.toppingName;
    }

    public void setName(String name) {
        this.toppingName = name;
    }

    // Compatibility builder extension
    public static class ToppingBuilder {
        public ToppingBuilder name(String name) {
            this.toppingName = name;
            return this;
        }
    }
}
