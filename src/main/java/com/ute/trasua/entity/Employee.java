package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "employee_code")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", nullable = false, length = 30)
    private String employeeCode;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "citizen_id", length = 20)
    private String citizenId;

    @Column(name = "email", length = 100)
    private String email;

    /**
     * Position: BARTENDER, CASHIER, SERVER, SHIFT_MANAGER, SECURITY
     */
    @Column(name = "position", length = 50)
    @Builder.Default
    private String position = "BARTENDER";

    /**
     * Work Shift: MORNING (06:30 - 12:00), AFTERNOON (12:00 - 17:30), EVENING (17:30 - 23:00), FULL_TIME
     */
    @Column(name = "shift", length = 50)
    @Builder.Default
    private String shift = "MORNING";

    @Column(name = "salary")
    private Double salary;

    /**
     * Status: ACTIVE, RESIGNED, ON_LEAVE
     */
    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (hireDate == null) {
            hireDate = LocalDate.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (position == null) {
            position = "BARTENDER";
        }
        if (shift == null) {
            shift = "MORNING";
        }
    }
}
