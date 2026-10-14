package com.ute.trasua.repository;

import com.ute.trasua.entity.ImportReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ImportReceiptDetail (Detail BM2 - Task 4.1 / Module 14).
 */
@Repository
public interface ImportReceiptDetailRepository extends JpaRepository<ImportReceiptDetail, Long> {

    List<ImportReceiptDetail> findByImportReceiptId(Long receiptId);

    @Query("SELECT d FROM ImportReceiptDetail d " +
           "JOIN FETCH d.ingredient " +
           "WHERE d.importReceipt.id = :receiptId")
    List<ImportReceiptDetail> findByImportReceiptIdWithIngredient(@Param("receiptId") Long receiptId);

    boolean existsByIngredientId(Long ingredientId);

    void deleteByImportReceiptId(Long receiptId);
}
