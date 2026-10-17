package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders",
       indexes = {
           @Index(name = "idx_orders_code", columnList = "order_code"),
           @Index(name = "idx_orders_created", columnList = "created_at"),
           @Index(name = "idx_orders_branch_status_driver", columnList = "branch_id,status,driver_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true, length = 50)
    private String orderCode;

    @Column(name = "daily_queue_number")
    private Integer dailyQueueNumber;

    @Column(name = "order_type", length = 30)
    private String orderType;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "final_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_status", length = 30)
    private String paymentStatus;

    @Column(name = "cashier_username", length = 50)
    private String cashierUsername;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "note", length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "distance_km", precision = 6, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "shipping_fee", precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "voucher_code", length = 50)
    private String voucherCode;

    @Column(name = "delivery_notes", length = 500)
    private String deliveryNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "handed_over_at")
    private LocalDateTime handedOverAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "incident_reason", length = 30)
    private String incidentReason;

    @Column(name = "incident_note", length = 255)
    private String incidentNote;

    @Column(name = "incident_reported_at")
    private LocalDateTime incidentReportedAt;

    @Column(name = "pickup_photo_url", columnDefinition = "LONGTEXT")
    private String pickupPhotoUrl;

    @Column(name = "delivery_photo_url", columnDefinition = "LONGTEXT")
    private String deliveryPhotoUrl;

    @Column(name = "failure_reason", length = 50)
    private String failureReason;

    @Column(name = "failure_proof_url", columnDefinition = "LONGTEXT")
    private String failureProofUrl;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<OrderDetail> orderDetails;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.orderType == null) {
            this.orderType = "POS_WALK_IN";
        }
        if (this.status == null) {
            this.status = "PENDING";
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = "UNPAID";
        }
        if (this.totalAmount == null) {
            this.totalAmount = BigDecimal.ZERO;
        }
        if (this.discountAmount == null) {
            this.discountAmount = BigDecimal.ZERO;
        }
        if (this.finalAmount == null) {
            this.finalAmount = BigDecimal.ZERO;
        }
        if (this.shippingFee == null) {
            this.shippingFee = BigDecimal.ZERO;
        }
        if (this.orderDetails == null) {
            this.orderDetails = new ArrayList<>();
        }
    }
}
