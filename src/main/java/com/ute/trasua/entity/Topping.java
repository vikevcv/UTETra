package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "toppings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";
}
