package com.ute.trasua.dto.pos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAttendanceResponse {

    private boolean shiftActive;
    private int activeCount;
    private String activeCashiersSummary;
    private List<ShiftEmployeeDto> activeEmployees;
}
