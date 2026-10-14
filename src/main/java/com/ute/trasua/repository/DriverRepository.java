package com.ute.trasua.repository;

import com.ute.trasua.entity.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByUserId(Long userId);

    Optional<Driver> findByUserUsername(String username);

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByLicensePlateAndIdNot(String licensePlate, Long id);

    List<Driver> findByBranchId(Long branchId);

    @Query("SELECT d FROM Driver d JOIN d.user u WHERE " +
           "d.branch.id = :branchId AND " +
           "(:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(d.licensePlate) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:workingStatus IS NULL OR d.workingStatus = :workingStatus) " +
           "ORDER BY d.id DESC")
    Page<Driver> searchDriversByBranch(@Param("branchId") Long branchId,
                                       @Param("keyword") String keyword,
                                       @Param("workingStatus") String workingStatus,
                                       Pageable pageable);

    long countByBranchId(Long branchId);

    long countByBranchIdAndWorkingStatus(Long branchId, String workingStatus);

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.branch.id = :branchId AND d.user.locked = true")
    long countSuspendedByBranchId(@Param("branchId") Long branchId);
}
