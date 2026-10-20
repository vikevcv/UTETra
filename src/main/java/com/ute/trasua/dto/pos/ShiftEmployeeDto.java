package com.ute.trasua.dto.pos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftEmployeeDto {

    private Long id;
    private String employeeCode;
    private String fullName;
    private String phone;
    private boolean activeOnShift;
}
