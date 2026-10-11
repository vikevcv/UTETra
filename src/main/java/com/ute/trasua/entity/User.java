package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 100)
    private String fullName;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String avatar;

    @Column(nullable = false)
    @Builder.Default
    private boolean locked = false;

    @Column(length = 255)
    private String lockReason;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles",
                     joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (roles.isEmpty()) {
            roles.add("ROLE_CUSTOMER");
        }
        if (createdAt == null) {
            createdAt = java.time.LocalDateTime.now();
        }
    }
}