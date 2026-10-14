package com.ute.trasua.dto;

import com.ute.trasua.entity.BranchInventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchInventoryResponse {

    private Long id;
    private Long branchId;
    private String branchName;
    private String branchCode;
    private Long ingredientId;
    private String ingredientCode;
    private String ingredientName;
    private String unit;
    private BigDecimal stockQuantity;
    private BigDecimal minThreshold;
    private boolean lowStock;
    private boolean outOfStock;
    private LocalDateTime updatedAt;

    public static BranchInventoryResponse fromEntity(BranchInventory b) {
        if (b == null) return null;

        Long bId = null;
        String bName = null;
        String bCode = null;
        if (b.getBranch() != null) {
            bId = b.getBranch().getId();
            bName = b.getBranch().getName();
            bCode = b.getBranch().getBranchCode();
        }

        Long ingId = null;
        String ingCode = null;
        String ingName = null;
        String unit = null;
        if (b.getIngredient() != null) {
            ingId = b.getIngredient().getId();
            ingCode = b.getIngredient().getCode();
            ingName = b.getIngredient().getName();
            unit = b.getIngredient().getUnit();
        }

        return BranchInventoryResponse.builder()
                .id(b.getId())
                .branchId(bId)
                .branchName(bName)
                .branchCode(bCode)
                .ingredientId(ingId)
                .ingredientCode(ingCode)
                .ingredientName(ingName)
                .unit(unit)
                .stockQuantity(b.getStockQuantity() != null ? b.getStockQuantity() : BigDecimal.ZERO)
                .minThreshold(b.getMinThreshold() != null ? b.getMinThreshold() : new BigDecimal("10.00"))
                .lowStock(b.isLowStock())
                .outOfStock(b.isOutOfStock())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
