package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Branch-scoped topping availability (UC11).
 * Stores whether a given topping is available at a specific branch,
 * instead of mutating the global {@link Topping#getStatus()}.
 */
@Entity
@Table(name = "branch_toppings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "topping_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchTopping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "topping_id", nullable = false)
    private Long toppingId;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;
}
