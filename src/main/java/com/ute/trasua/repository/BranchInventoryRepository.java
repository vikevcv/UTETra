package com.ute.trasua.repository;

import com.ute.trasua.entity.BranchInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BranchInventory (Task 4.1 / Module 14).
 * Manages branch stock quantities and shortage alerts.
 */
@Repository
public interface BranchInventoryRepository extends JpaRepository<BranchInventory, Long> {

    Optional<BranchInventory> findByBranchIdAndIngredientId(Long branchId, Long ingredientId);

    /**
     * Lock row for update (PESSIMISTIC_WRITE) to prevent race condition & lost updates during deduction or import.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BranchInventory b WHERE b.branch.id = :branchId AND b.ingredient.id = :ingredientId")
    Optional<BranchInventory> findByBranchIdAndIngredientIdWithLock(@Param("branchId") Long branchId, @Param("ingredientId") Long ingredientId);

    boolean existsByBranchIdAndIngredientId(Long branchId, Long ingredientId);

    List<BranchInventory> findByBranchId(Long branchId);

    List<BranchInventory> findByBranchBranchCode(String branchCode);

    @Query("SELECT b FROM BranchInventory b " +
           "JOIN FETCH b.ingredient i " +
           "WHERE b.branch.id = :branchId AND " +
           "(:keyword IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:isLowStock IS NULL OR :isLowStock = false OR b.stockQuantity <= b.minThreshold) " +
           "ORDER BY i.name ASC")
    List<BranchInventory> searchStockByBranch(@Param("branchId") Long branchId,
                                              @Param("keyword") String keyword,
                                              @Param("isLowStock") Boolean isLowStock);

    @Query("SELECT b FROM BranchInventory b " +
           "JOIN FETCH b.ingredient i " +
           "WHERE b.branch.id = :branchId AND " +
           "(:keyword IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:isLowStock IS NULL OR :isLowStock = false OR b.stockQuantity <= b.minThreshold) " +
           "ORDER BY i.name ASC")
    Page<BranchInventory> searchStockByBranchPaged(@Param("branchId") Long branchId,
                                                   @Param("keyword") String keyword,
                                                   @Param("isLowStock") Boolean isLowStock,
                                                   Pageable pageable);

    @Query("SELECT COUNT(b) FROM BranchInventory b WHERE b.branch.id = :branchId AND b.stockQuantity <= b.minThreshold")
    long countLowStockByBranch(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(b) FROM BranchInventory b WHERE b.branch.id = :branchId AND b.stockQuantity <= 0")
    long countOutOfStockByBranch(@Param("branchId") Long branchId);
}
