package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Thực thể lưu trữ Sổ địa chỉ giao hàng của khách hàng (Delivery Address Book).
 * Giúp khách hàng lưu trữ các địa chỉ nhận hàng thân quen (nhà riêng, cơ quan, trường học...)
 * để đặt hàng nhanh chóng trong 1 thao tác.
 */
@Entity
@Table(name = "user_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    /**
     * Nhãn gợi nhớ địa chỉ (Ví dụ: "Nhà riêng", "Công ty", "Trường học", "Ký túc xá", "Khác")
     */
    @Column(nullable = false, length = 50)
    private String label;

    /**
     * Tên người nhận hàng tại địa chỉ này
     */
    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    /**
     * Số điện thoại liên hệ nhận hàng
     */
    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    /**
     * Địa chỉ giao hàng chi tiết (Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành)
     */
    @Column(name = "address_detail", nullable = false, length = 255)
    private String addressDetail;

    /**
     * Tọa độ GPS phục vụ tính toán khoảng cách Haversine và định tuyến tài xế
     */
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    /**
     * Cờ đánh dấu địa chỉ mặc định (ưu tiên chọn sẵn khi vào Checkout)
     */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
