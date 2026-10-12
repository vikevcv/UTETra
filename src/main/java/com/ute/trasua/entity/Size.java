package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sizes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Size {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "volume_ml")
    private Integer volumeMl;

    @Column(name = "extra_price", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal extraPrice = BigDecimal.ZERO;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
