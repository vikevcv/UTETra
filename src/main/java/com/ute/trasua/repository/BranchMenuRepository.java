package com.ute.trasua.repository;

import com.ute.trasua.entity.BranchMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BranchMenu (UC22).
 * Handles branch-specific menu item querying, availability toggling, and stock tracking.
 */
@Repository
public interface BranchMenuRepository extends JpaRepository<BranchMenu, Long> {

    List<BranchMenu> findByBranchId(Long branchId);

    List<BranchMenu> findByBranchIdAndAvailableTrue(Long branchId);

    List<BranchMenu> findByBranchIdAndAvailableFalse(Long branchId);

    Optional<BranchMenu> findByBranchIdAndProductId(Long branchId, Long productId);

    boolean existsByBranchIdAndProductId(Long branchId, Long productId);

    @Query(value = "SELECT bm FROM BranchMenu bm " +
           "JOIN FETCH bm.product p " +
           "LEFT JOIN FETCH p.category c " +
           "WHERE bm.branch.id = :branchId AND " +
           "UPPER(p.status) = 'ACTIVE' AND " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:available IS NULL OR bm.available = :available) " +
           "ORDER BY c.displayOrder ASC, p.name ASC",
           countQuery = "SELECT COUNT(bm) FROM BranchMenu bm " +
           "JOIN bm.product p " +
           "WHERE bm.branch.id = :branchId AND " +
           "UPPER(p.status) = 'ACTIVE' AND " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:available IS NULL OR bm.available = :available)")
    Page<BranchMenu> searchBranchMenus(@Param("branchId") Long branchId,
                                       @Param("keyword") String keyword,
                                       @Param("categoryId") Long categoryId,
                                       @Param("available") Boolean available,
                                       Pageable pageable);

    @Modifying
    @Query("UPDATE BranchMenu bm SET bm.available = :available WHERE bm.branch.id = :branchId AND bm.product.id = :productId")
    int updateAvailability(@Param("branchId") Long branchId,
                           @Param("productId") Long productId,
                           @Param("available") boolean available);

    @Query("SELECT COUNT(bm) FROM BranchMenu bm JOIN bm.product p WHERE bm.branch.id = :branchId AND UPPER(p.status) = 'ACTIVE'")
    long countActiveProductsByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(bm) FROM BranchMenu bm JOIN bm.product p WHERE bm.branch.id = :branchId AND UPPER(p.status) = 'ACTIVE' AND bm.available = true")
    long countActiveAndAvailableByBranchId(@Param("branchId") Long branchId);

    long countByBranchIdAndAvailableTrue(Long branchId);

    long countByBranchId(Long branchId);

    void deleteByBranchIdAndProductId(Long branchId, Long productId);

    @Modifying
    @Query("UPDATE BranchMenu bm SET bm.available = :available WHERE bm.product.id = :productId")
    int updateAvailabilityByProductId(@Param("productId") Long productId,
                                      @Param("available") boolean available);

    List<BranchMenu> findByProductId(Long productId);

    @Modifying
    @Query("DELETE FROM BranchMenu bm WHERE bm.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
