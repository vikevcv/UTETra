package com.ute.trasua.service;

import com.ute.trasua.dto.EmployeeRequest;
import com.ute.trasua.dto.EmployeeResponse;
import com.ute.trasua.dto.PosAccountRequest;
import com.ute.trasua.dto.PosAccountResponse;
import com.ute.trasua.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface handling Employee management and POS accounts at Branch (UC16).
 */
public interface EmployeeService {

    EmployeeResponse getEmployeeById(Long id);

    Employee getEmployeeEntityById(Long id);

    Page<EmployeeResponse> searchEmployees(Long branchId, String keyword, String status, Pageable pageable);

    List<EmployeeResponse> getAllByBranch(Long branchId);

    EmployeeResponse createEmployee(EmployeeRequest request);

    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);

    void updateStatus(Long id, String status);

    void deleteEmployee(Long id);

    long countByBranch(Long branchId);

    long countActiveByBranch(Long branchId);

    List<PosAccountResponse> getPosAccountsByBranch(Long branchId);

    PosAccountResponse createPosAccount(PosAccountRequest request);

    boolean togglePosAccount(Long id);

    void deletePosAccount(Long id);
}
