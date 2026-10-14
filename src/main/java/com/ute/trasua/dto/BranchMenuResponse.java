package com.ute.trasua.dto;

import com.ute.trasua.entity.BranchMenu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchMenuResponse {

    private Long id;
    private Long branchId;
    private String branchName;
    private String branchCode;

    private Long productId;
    private String productCode;
    private String productName;
    private String categoryName;
    private BigDecimal basePrice;
    private BigDecimal customPrice;
    private BigDecimal effectivePrice;
    private String formattedPrice;

    private String imageUrl;
    private boolean available;
    private LocalDateTime updatedAt;

    public static BranchMenuResponse fromEntity(BranchMenu bm) {
        if (bm == null) return null;

        Long bId = null;
        String bName = null;
        String bCode = null;
        if (bm.getBranch() != null) {
            bId = bm.getBranch().getId();
            bName = bm.getBranch().getName();
            bCode = bm.getBranch().getBranchCode();
        }

        Long pId = null;
        String pCode = null;
        String pName = null;
        String cName = null;
        BigDecimal baseP = BigDecimal.ZERO;
        String img = null;

        if (bm.getProduct() != null) {
            pId = bm.getProduct().getId();
            pCode = bm.getProduct().getProductCode();
            pName = bm.getProduct().getName();
            baseP = bm.getProduct().getBasePrice();
            img = bm.getProduct().getImageUrl();
            if (bm.getProduct().getCategory() != null) {
                cName = bm.getProduct().getCategory().getName();
            }
        }

        BigDecimal effP = bm.getCustomPrice() != null ? bm.getCustomPrice() : baseP;
        DecimalFormat df = new DecimalFormat("#,### đ");
        String formatted = effP != null ? df.format(effP) : "0 đ";

        return BranchMenuResponse.builder()
                .id(bm.getId())
                .branchId(bId)
                .branchName(bName)
                .branchCode(bCode)
                .productId(pId)
                .productCode(pCode)
                .productName(pName)
                .categoryName(cName)
                .basePrice(baseP)
                .customPrice(bm.getCustomPrice())
                .effectivePrice(effP)
                .formattedPrice(formatted)
                .imageUrl(img)
                .available(bm.isAvailable())
                .updatedAt(bm.getUpdatedAt())
                .build();
    }
}
