package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_detail_toppings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailTopping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false)
    @ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private OrderDetail orderDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topping_id")
    private Topping topping;

    @Column(name = "topping_name", nullable = false, length = 100)
    private String toppingName;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
}
