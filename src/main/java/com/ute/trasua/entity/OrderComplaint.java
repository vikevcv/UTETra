package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for Order Complaints filed by customers after order completion.
 * Strictly text-based (no image uploads).
 */
@Entity
@Table(name = "order_complaints",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_complaint_order", columnNames = {"order_id"})
       },
       indexes = {
           @Index(name = "idx_complaint_status", columnList = "status"),
           @Index(name = "idx_complaint_created", columnList = "created_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderComplaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    /**
     * SAI_MON, THIEU_MON, HU_HAI, CHAT_LUONG, THAI_DO, KHAC
     */
    @Column(name = "complaint_type", nullable = false, length = 50)
    private String complaintType;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    /**
     * PENDING: Đang chờ quán xử lý
     * RESOLVED: Quán chấp thuận bồi hoàn voucher
     * REJECTED: Quán từ chối
     */
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "compensation_voucher_code", length = 50)
    private String compensationVoucherCode;

    @Column(name = "compensation_amount", precision = 12, scale = 2)
    private BigDecimal compensationAmount;

    @Column(name = "admin_response", length = 500)
    private String adminResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    @ToString.Exclude
    private User resolvedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }
}
