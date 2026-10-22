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

    @Query("SELECT d FROM Driver d LEFT JOIN FETCH d.user LEFT JOIN FETCH d.branch WHERE d.user.username = :username")
    Optional<Driver> findByUserUsernameWithUserAndBranch(@Param("username") String username);

    @Query("SELECT d FROM Driver d LEFT JOIN FETCH d.user LEFT JOIN FETCH d.branch")
    List<Driver> findAllWithUserAndBranch();

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByLicensePlateAndIdNot(String licensePlate, Long id);

    List<Driver> findByBranchId(Long branchId);

    @Query("SELECT d FROM Driver d LEFT JOIN FETCH d.user WHERE d.branch.id = :branchId")
    List<Driver> findByBranchIdWithUser(@Param("branchId") Long branchId);

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

    @Query(value = "SELECT d FROM Driver d LEFT JOIN FETCH d.user u LEFT JOIN FETCH d.branch b WHERE " +
           "(:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(d.licensePlate) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY d.id DESC",
           countQuery = "SELECT COUNT(d) FROM Driver d LEFT JOIN d.user u LEFT JOIN d.branch b WHERE " +
           "(:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(d.licensePlate) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Driver> searchAllDrivers(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT d FROM Driver d LEFT JOIN FETCH d.user u LEFT JOIN FETCH d.branch b " +
           "WHERE (d.user IS NULL OR d.user.locked = false) " +
           "ORDER BY CASE WHEN d.workingStatus = 'ONLINE' THEN 1 WHEN d.workingStatus = 'DELIVERING' THEN 2 ELSE 3 END, d.id DESC")
    List<Driver> findTopActiveDrivers(Pageable pageable);

    // --- Thống kê toàn chuỗi (Admin) ---
    @Query("SELECT COUNT(d) FROM Driver d")
    long countTotalDrivers();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.workingStatus = 'ONLINE' AND (d.user IS NULL OR d.user.locked = false)")
    long countActiveOnlineDrivers();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.workingStatus = 'DELIVERING' AND (d.user IS NULL OR d.user.locked = false)")
    long countActiveDeliveringDrivers();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.workingStatus = 'OFFLINE' AND (d.user IS NULL OR d.user.locked = false)")
    long countActiveOfflineDrivers();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.user IS NOT NULL AND d.user.locked = true")
    long countSuspendedDrivers();

    // --- Thống kê cấp chi nhánh (Branch) ---
    long countByBranchId(Long branchId);

    long countByBranchIdAndWorkingStatus(Long branchId, String workingStatus);

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.branch.id = :branchId AND d.workingStatus = 'ONLINE' AND (d.user IS NULL OR d.user.locked = false)")
    long countActiveOnlineByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.branch.id = :branchId AND d.workingStatus = 'DELIVERING' AND (d.user IS NULL OR d.user.locked = false)")
    long countActiveDeliveringByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.branch.id = :branchId AND d.workingStatus = 'OFFLINE' AND (d.user IS NULL OR d.user.locked = false)")
    long countActiveOfflineByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.branch.id = :branchId AND d.user IS NOT NULL AND d.user.locked = true")
    long countSuspendedByBranchId(@Param("branchId") Long branchId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Driver d " +
           "WHERE d.branch.id = :branchId " +
           "AND d.workingStatus IN ('ONLINE', 'DELIVERING') " +
           "AND (d.user IS NULL OR d.user.locked = false)")
    List<Driver> lockEligibleDriversForBranch(@Param("branchId") Long branchId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Driver d WHERE d.id = :driverId")
    Optional<Driver> findWithLockById(@Param("driverId") Long driverId);
}
