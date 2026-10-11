package com.ute.trasua.dto;

import com.ute.trasua.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {

    private Long id;
    private String employeeCode;
    private String fullName;
    private String phone;
    private String citizenId;
    private String email;
    private String position;
    private String shift;
    private Double salary;
    private String status;
    private LocalDate hireDate;
    private LocalDateTime createdAt;

    private Long branchId;
    private String branchCode;
    private String branchName;

    public static EmployeeResponse fromEntity(Employee emp) {
        if (emp == null) return null;

        Long bId = null;
        String bCode = null;
        String bName = null;

        if (emp.getBranch() != null) {
            bId = emp.getBranch().getId();
            bCode = emp.getBranch().getBranchCode();
            bName = emp.getBranch().getName();
        }

        return EmployeeResponse.builder()
                .id(emp.getId())
                .employeeCode(emp.getEmployeeCode())
                .fullName(emp.getFullName())
                .phone(emp.getPhone())
                .citizenId(emp.getCitizenId())
                .email(emp.getEmail())
                .position(emp.getPosition())
                .shift(emp.getShift())
                .salary(emp.getSalary())
                .status(emp.getStatus())
                .hireDate(emp.getHireDate())
                .createdAt(emp.getCreatedAt())
                .branchId(bId)
                .branchCode(bCode)
                .branchName(bName)
                .build();
    }

    public String getPositionTitle() {
        if (position == null) return "Chưa xác định";
        return switch (position) {
            case "BARTENDER" -> "Pha Chế";
            case "CASHIER" -> "Thu Ngân";
            case "SERVER" -> "Phục Vụ";
            case "SHIFT_MANAGER" -> "Quản Lý Ca";
            case "SECURITY" -> "Bảo Vệ";
            default -> position;
        };
    }

    public String getPositionBadgeClass() {
        if (position == null) return "zen-badge-secondary";
        return switch (position) {
            case "BARTENDER" -> "zen-badge-primary";
            case "CASHIER" -> "zen-badge-info";
            case "SERVER" -> "zen-badge-warning";
            case "SHIFT_MANAGER" -> "zen-badge-danger";
            default -> "zen-badge-secondary";
        };
    }

    public String getShiftTitle() {
        if (shift == null) return "Chưa xếp ca";
        return switch (shift) {
            case "MORNING" -> "Ca Sáng (06:30 - 12:00)";
            case "AFTERNOON" -> "Ca Chiều (12:00 - 17:30)";
            case "EVENING" -> "Ca Tối (17:30 - 23:00)";
            case "FULL_TIME" -> "Toàn Thời Gian (Full-time)";
            default -> shift;
        };
    }

    public String getStatusTitle() {
        if ("RESIGNED".equals(status)) return "Đã nghỉ việc";
        if ("ON_LEAVE".equals(status)) return "Tạm nghỉ phép";
        return "Đang làm việc";
    }

    public String getStatusBadgeClass() {
        if ("RESIGNED".equals(status)) return "zen-badge-secondary";
        if ("ON_LEAVE".equals(status)) return "zen-badge-warning";
        return "zen-badge-success";
    }

    public String getFormattedSalary() {
        if (salary == null || salary <= 0) return "Chưa thiết lập";
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(salary) + " ₫";
    }

    public String getFormattedHireDate() {
        if (hireDate == null) return "Chưa cập nhật";
        return hireDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
