package com.ute.trasua.service;

import com.ute.trasua.dto.BranchMenuResponse;
import com.ute.trasua.dto.ProductBranchMatrixRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for branch-level menu availability and custom pricing (UC22).
 */
public interface BranchMenuService {

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
}
