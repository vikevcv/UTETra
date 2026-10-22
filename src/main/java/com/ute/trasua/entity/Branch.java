package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "branches",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "branch_code")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_code", nullable = false, length = 30)
    private String branchCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "hotline", length = 20)
    private String hotline;

    @Column(name = "open_time", length = 10)
    @Builder.Default
    private String openTime = "07:00";

    @Column(name = "close_time", length = 10)
    @Builder.Default
    private String closeTime = "22:30";

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @ToString.Exclude
    // Cắt vòng đệ quy Branch.hashCode() -> User.hashCode() -> User.branch -> Branch.hashCode()
    // (Hibernate băm phần tử collection khi flush -> StackOverflowError nếu để vòng này)
    @lombok.EqualsAndHashCode.Exclude
    private User manager;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (openTime == null) {
            openTime = "07:00";
        }
        if (closeTime == null) {
            closeTime = "22:30";
        }
    }
}
