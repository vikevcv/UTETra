package com.ute.trasua.service;

import com.ute.trasua.dto.BranchInventoryResponse;
import com.ute.trasua.dto.ImportReceiptRequest;
import com.ute.trasua.dto.ImportReceiptResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for Branch Inventory and BM2 Import Receipt management (Task 4.2 / Module 14).
 */
public interface InventoryService {

    List<BranchInventoryResponse> getStockByBranch(Long branchId, String keyword, Boolean isLowStock);

    Page<BranchInventoryResponse> getStockByBranchPaged(Long branchId, String keyword, Boolean isLowStock, Pageable pageable);

    BranchInventoryResponse updateMinThreshold(Long branchId, Long ingredientId, BigDecimal minThreshold);

    long countLowStock(Long branchId);

    long countOutOfStock(Long branchId);

    ImportReceiptResponse createImportReceipt(Long branchId, String username, ImportReceiptRequest request);

    List<ImportReceiptResponse> getReceiptsByBranch(Long branchId, String keyword, String status, LocalDateTime startDate, LocalDateTime endDate);

    Page<ImportReceiptResponse> getReceiptsByBranchPaged(Long branchId, String keyword, String status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    ImportReceiptResponse getReceiptById(Long id);

    ImportReceiptResponse getReceiptByCode(String receiptCode);

    com.ute.trasua.entity.BranchInventory deductIngredientStock(Long branchId, Long ingredientId, BigDecimal quantity);
}
