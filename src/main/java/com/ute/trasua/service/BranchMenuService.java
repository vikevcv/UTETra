package com.ute.trasua.service;

import com.ute.trasua.dto.BranchMenuResponse;
import com.ute.trasua.dto.ProductBranchMatrixRow;
import com.ute.trasua.dto.kitchen.BranchMenuStatusDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service interface for branch-level menu availability, custom pricing (UC22),
 * and kitchen / barista status toggling & cup inventory deduction (UC11).
 */
public interface BranchMenuService {

    // ==========================================
    // UC22: Branch Menu Management & Matrix
    // ==========================================

    List<BranchMenuResponse> getMenuByBranch(Long branchId);

    Page<BranchMenuResponse> searchBranchMenu(Long branchId,
                                             String keyword,
                                             Long categoryId,
                                             Boolean available,
                                             Pageable pageable);

    List<ProductBranchMatrixRow> getProductBranchMatrix(String keyword, Long categoryId);

    boolean updateProductAvailability(Long branchId, Long productId, boolean available);

    boolean updateCustomPrice(Long branchId, Long productId, BigDecimal customPrice);

    void syncAllProductsToBranch(Long branchId);

    void syncAllProductsToAllBranches();

    long countAvailableAtBranch(Long branchId);

    long countTotalAtBranch(Long branchId);

    // ==========================================
    // UC11: Kitchen / Barista KDS Status & Stocks
    // ==========================================

    List<BranchMenuStatusDto> getBranchMenuStatus(Long branchId);

    boolean toggleProductAvailability(Long branchId, Long productId, Boolean isAvailable);

    boolean toggleToppingAvailability(Long branchId, Long toppingId, Boolean isAvailable);

    boolean isProductAvailable(Long branchId, Long productId);

    Map<String, Integer> getBranchCupStocks(Long branchId);

    void deductCupStock(Long branchId, String sizeName, int quantity);

    void deductProductStock(Long branchId, Long productId, int quantity);

    void deductToppingStock(Long branchId, String toppingName, int quantity);
}
