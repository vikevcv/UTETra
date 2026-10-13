package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequest {

    private Long id;

    private String employeeCode;

    private String fullName;

    private String phone;

    private String citizenId;

    private String email;

    private Double salary;

    @Builder.Default
    private String status = "ACTIVE";

    private LocalDate hireDate;

    private Long branchId;
}
