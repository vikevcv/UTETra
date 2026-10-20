package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for Customer Product Review & Rating (UC07).
 * Strictly text & star rating based (no image attachments).
 */
@Entity
@Table(name = "product_reviews",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_order_order_detail", columnNames = {"order_id", "order_detail_id"})
       },
       indexes = {
           @Index(name = "idx_reviews_product", columnList = "product_id"),
           @Index(name = "idx_reviews_order", columnList = "order_id"),
           @Index(name = "idx_reviews_user", columnList = "user_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false)
    @ToString.Exclude
    private OrderDetail orderDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1 to 5

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "is_hidden", nullable = false)
    @Builder.Default
    private Boolean isHidden = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isHidden == null) {
            this.isHidden = false;
        }
        if (this.rating == null) {
            this.rating = 5;
        } else if (this.rating < 1) {
            this.rating = 1;
        } else if (this.rating > 5) {
            this.rating = 5;
        }
    }
}
