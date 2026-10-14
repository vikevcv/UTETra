package com.ute.trasua.repository;

import com.ute.trasua.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCodeAndIdNot(String employeeCode, Long id);

    List<Employee> findByBranchId(Long branchId);

    List<Employee> findByBranchIdAndStatus(Long branchId, String status);

    long countByBranchId(Long branchId);

    long countByBranchIdAndStatus(Long branchId, String status);

    @Query("SELECT e FROM Employee e JOIN FETCH e.branch b WHERE " +
           "(:branchId IS NULL OR b.id = :branchId) AND " +
           "(:status IS NULL OR :status = '' OR e.status = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(e.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(e.citizenId) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Employee> searchEmployees(@Param("branchId") Long branchId,
                                   @Param("keyword") String keyword,
                                   @Param("status") String status,
                                   Pageable pageable);
}
