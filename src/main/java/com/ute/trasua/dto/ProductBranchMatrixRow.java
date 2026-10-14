package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO representing a row in the Product x Branch availability matrix (UC22).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductBranchMatrixRow {

    private ProductResponse product;

    /**
     * Map from branchId to BranchMenuResponse (holding availability and custom price).
     */
    @Builder.Default
    private Map<Long, BranchMenuResponse> branchMenus = new HashMap<>();

    public boolean isAvailableAt(Long branchId) {
        BranchMenuResponse bm = branchMenus.get(branchId);
        return bm != null && bm.isAvailable();
    }
}
