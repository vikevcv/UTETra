package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Thực đơn chi nhánh (Bảng 3.11 SE Report: branch_menus).
 * Quản lý trạng thái còn/hết hàng của từng sản phẩm tại từng cơ sở chi nhánh.
 */
@Entity
@Table(name = "branch_menus",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_branch_product", columnNames = {"branch_id", "product_id"})
       },
       indexes = {
           @Index(name = "idx_branch_menu_branch", columnList = "branch_id"),
           @Index(name = "idx_branch_menu_available", columnList = "is_available")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Tình trạng còn hàng tại quán: true = Còn hàng; false = Tạm hết hàng tại quán.
     */
    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
