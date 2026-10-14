package com.ute.trasua.dto;

import com.ute.trasua.entity.ImportReceipt;
import com.ute.trasua.entity.ImportReceiptDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportReceiptResponse {

    private Long id;
    private String receiptCode;
    private Long branchId;
    private String branchName;
    private String branchCode;
    private LocalDateTime importDate;
    private String supplierType;
    private String supplierTypeName;
    private String supplierName;
    private String deliveredBy;
    private String receivedBy;
    private String createdByUsername;
    private String createdByFullName;
    private BigDecimal totalAmount;
    private String formattedTotalAmount;
    private String status;
    private String statusName;
    private String note;
    private LocalDateTime createdAt;

    @Builder.Default
    private List<ItemResponse> items = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemResponse {
        private Long id;
        private Long ingredientId;
        private String ingredientCode;
        private String ingredientName;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private String formattedUnitPrice;
        private BigDecimal totalPrice;
        private String formattedTotalPrice;
        private LocalDate expiryDate;
        private String note;

        public static ItemResponse fromEntity(ImportReceiptDetail d) {
            if (d == null) return null;
            DecimalFormat df = new DecimalFormat("#,### đ");

            Long ingId = null;
            String ingCode = null;
            String ingName = null;
            String unit = null;
            if (d.getIngredient() != null) {
                ingId = d.getIngredient().getId();
                ingCode = d.getIngredient().getCode();
                ingName = d.getIngredient().getName();
                unit = d.getIngredient().getUnit();
            }

            return ItemResponse.builder()
                    .id(d.getId())
                    .ingredientId(ingId)
                    .ingredientCode(ingCode)
                    .ingredientName(ingName)
                    .unit(unit)
                    .quantity(d.getQuantity())
                    .unitPrice(d.getUnitPrice())
                    .formattedUnitPrice(d.getUnitPrice() != null ? df.format(d.getUnitPrice()) : "0 đ")
                    .totalPrice(d.getTotalPrice())
                    .formattedTotalPrice(d.getTotalPrice() != null ? df.format(d.getTotalPrice()) : "0 đ")
                    .expiryDate(d.getExpiryDate())
                    .note(d.getNote())
                    .build();
        }
    }

    public static ImportReceiptResponse fromEntity(ImportReceipt r) {
        if (r == null) return null;
        DecimalFormat df = new DecimalFormat("#,### đ");

        Long bId = null;
        String bName = null;
        String bCode = null;
        if (r.getBranch() != null) {
            bId = r.getBranch().getId();
            bName = r.getBranch().getName();
            bCode = r.getBranch().getBranchCode();
        }

        String username = null;
        String fullName = null;
        if (r.getCreatedBy() != null) {
            username = r.getCreatedBy().getUsername();
            fullName = r.getCreatedBy().getFullName();
        }

        String supTypeName = "Kho tổng chuỗi";
        if ("AUTHORIZED_SUPPLIER".equalsIgnoreCase(r.getSupplierType()) || "NhaCungCapUyQuyen".equalsIgnoreCase(r.getSupplierType())) {
            supTypeName = "Nhà cung cấp ủy quyền";
        }

        String stName = "Đã nhập kho";
        if ("PENDING".equalsIgnoreCase(r.getStatus()) || "ChoXacNhan".equalsIgnoreCase(r.getStatus())) {
            stName = "Chờ xác nhận";
        }

        List<ItemResponse> itemResponses = new ArrayList<>();
        if (r.getDetails() != null) {
            itemResponses = r.getDetails().stream()
                    .map(ItemResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        return ImportReceiptResponse.builder()
                .id(r.getId())
                .receiptCode(r.getReceiptCode())
                .branchId(bId)
                .branchName(bName)
                .branchCode(bCode)
                .importDate(r.getImportDate())
                .supplierType(r.getSupplierType())
                .supplierTypeName(supTypeName)
                .supplierName(r.getSupplierName())
                .deliveredBy(r.getDeliveredBy())
                .receivedBy(r.getReceivedBy())
                .createdByUsername(username)
                .createdByFullName(fullName)
                .totalAmount(r.getTotalAmount())
                .formattedTotalAmount(r.getTotalAmount() != null ? df.format(r.getTotalAmount()) : "0 đ")
                .status(r.getStatus())
                .statusName(stName)
                .note(r.getNote())
                .createdAt(r.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}
