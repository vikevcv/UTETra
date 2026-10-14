package com.ute.trasua.repository;

import com.ute.trasua.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    boolean existsByVoucherCode(String voucherCode);

    boolean existsByVoucherCodeAndIdNot(String voucherCode, Long id);

    Optional<Promotion> findByVoucherCode(String voucherCode);

    List<Promotion> findByBranchId(Long branchId);

    @Query("SELECT p FROM Promotion p LEFT JOIN p.branch b WHERE " +
           "(:keyword IS NULL OR LOWER(p.voucherCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:scope IS NULL OR p.scope = :scope) AND " +
           "(:branchId IS NULL OR (b.id = :branchId)) AND " +
           "(:status IS NULL OR p.status = :status) " +
           "ORDER BY p.id DESC")
    Page<Promotion> searchForAdmin(@Param("keyword") String keyword,
                                   @Param("scope") String scope,
                                   @Param("branchId") Long branchId,
                                   @Param("status") String status,
                                   Pageable pageable);

    @Query("SELECT p FROM Promotion p WHERE " +
           "p.scope = 'BRANCH' AND p.branch.id = :branchId AND " +
           "(:keyword IS NULL OR LOWER(p.voucherCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR p.status = :status) " +
           "ORDER BY p.id DESC")
    Page<Promotion> searchForBranch(@Param("branchId") Long branchId,
                                    @Param("keyword") String keyword,
                                    @Param("status") String status,
                                    Pageable pageable);

    @Query("SELECT p FROM Promotion p WHERE " +
           "p.status = 'ACTIVE' AND " +
           "(:now BETWEEN p.startDate AND p.endDate) AND " +
           "(p.usedCount < p.totalUsageLimit) AND " +
           "(p.scope = 'GLOBAL' OR (p.scope = 'BRANCH' AND p.branch.id = :branchId)) " +
           "ORDER BY p.discountValue DESC")
    List<Promotion> findActivePromotionsForStore(@Param("branchId") Long branchId,
                                                 @Param("now") LocalDateTime now);

    long countByBranchId(Long branchId);

    long countByScope(String scope);

    long countByStatus(String status);

    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.status = 'ACTIVE' AND (:now BETWEEN p.startDate AND p.endDate)")
    long countCurrentlyActive(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.scope = 'BRANCH' AND p.branch.id = :branchId AND p.status = 'ACTIVE' AND (:now BETWEEN p.startDate AND p.endDate)")
    long countCurrentlyActiveByBranch(@Param("branchId") Long branchId, @Param("now") LocalDateTime now);
}
