package com.ute.trasua.repository;

import com.ute.trasua.entity.ImportReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ImportReceipt (Master BM2 - Task 4.1 / Module 14).
 * Manages branch ingredient import receipts (QLCN_BM2).
 */
@Repository
public interface ImportReceiptRepository extends JpaRepository<ImportReceipt, Long> {

    Optional<ImportReceipt> findByReceiptCode(String receiptCode);

    boolean existsByReceiptCode(String receiptCode);

    List<ImportReceipt> findByBranchIdOrderByImportDateDesc(Long branchId);

    @Query("SELECT r FROM ImportReceipt r " +
           "JOIN FETCH r.branch b " +
           "LEFT JOIN FETCH r.createdBy u " +
           "WHERE (:branchId IS NULL OR r.branch.id = :branchId) AND " +
           "(:keyword IS NULL OR LOWER(r.receiptCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(r.deliveredBy) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(r.receivedBy) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(r.supplierName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:startDate IS NULL OR r.importDate >= :startDate) AND " +
           "(:endDate IS NULL OR r.importDate <= :endDate) " +
           "ORDER BY r.importDate DESC")
    List<ImportReceipt> searchReceipts(@Param("branchId") Long branchId,
                                       @Param("keyword") String keyword,
                                       @Param("status") String status,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT r FROM ImportReceipt r " +
           "JOIN FETCH r.branch b " +
           "LEFT JOIN FETCH r.createdBy u " +
           "WHERE (:branchId IS NULL OR r.branch.id = :branchId) AND " +
           "(:keyword IS NULL OR LOWER(r.receiptCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(r.deliveredBy) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(r.receivedBy) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(r.supplierName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:startDate IS NULL OR r.importDate >= :startDate) AND " +
           "(:endDate IS NULL OR r.importDate <= :endDate) " +
           "ORDER BY r.importDate DESC",
           countQuery = "SELECT COUNT(r) FROM ImportReceipt r " +
           "WHERE (:branchId IS NULL OR r.branch.id = :branchId) AND " +
           "(:keyword IS NULL OR LOWER(r.receiptCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(r.deliveredBy) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(r.receivedBy) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(r.supplierName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:startDate IS NULL OR r.importDate >= :startDate) AND " +
           "(:endDate IS NULL OR r.importDate <= :endDate)")
    Page<ImportReceipt> searchReceiptsPaged(@Param("branchId") Long branchId,
                                           @Param("keyword") String keyword,
                                           @Param("status") String status,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           Pageable pageable);

    @Query("SELECT r FROM ImportReceipt r " +
           "LEFT JOIN FETCH r.details d " +
           "LEFT JOIN FETCH d.ingredient " +
           "WHERE r.id = :id")
    Optional<ImportReceipt> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM ImportReceipt r " +
           "LEFT JOIN FETCH r.details d " +
           "LEFT JOIN FETCH d.ingredient " +
           "WHERE r.receiptCode = :receiptCode")
    Optional<ImportReceipt> findByReceiptCodeWithDetails(@Param("receiptCode") String receiptCode);
}
