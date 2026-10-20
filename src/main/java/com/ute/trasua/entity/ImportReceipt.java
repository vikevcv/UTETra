package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing branch ingredient import receipts following BM2 standard (Master BM2 - Task 4.1 / Module 14).
 * Template reference: QLCN_BM2: PHIẾU NHẬP NGUYÊN LIỆU CHI NHÁNH.
 * Table: import_receipts (Reference Table 3.19 in report).
 */
@Entity
@Table(
    name = "import_receipts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_import_receipt_code", columnNames = {"receipt_code"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique receipt code (e.g. PNK-TD01-20261013-001).
     */
    @Column(name = "receipt_code", nullable = false, length = 50)
    private String receiptCode;

    /**
     * Branch receiving the materials.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    /**
     * Import timestamp.
     */
    @Column(name = "import_date", nullable = false)
    private LocalDateTime importDate;

    /**
     * Supplier source: 'CENTRAL_WAREHOUSE' (Kho tổng chuỗi), 'AUTHORIZED_SUPPLIER' (Nhà cung cấp ủy quyền).
     */
    @Column(name = "supplier_type", length = 50)
    @Builder.Default
    private String supplierType = "CENTRAL_WAREHOUSE";

    /**
     * Supplier or delivery vendor name.
     */
    @Column(name = "supplier_name", length = 150)
    private String supplierName;

    /**
     * Full name of driver / delivery person (signature verification).
     */
    @Column(name = "delivered_by", nullable = false, length = 100)
    private String deliveredBy;

    /**
     * Full name of branch manager receiving goods (signature verification).
     */
    @Column(name = "received_by", nullable = false, length = 100)
    private String receivedBy;

    /**
     * User account of branch manager creating receipt.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    /**
     * Total receipt amount (VNĐ) = sum of line item subtotals.
     */
    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * Status: 'IMPORTED' (Đã nhập & cộng tồn kho), 'PENDING' (Chờ xác nhận).
     */
    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "IMPORTED";

    /**
     * Inspection note, package conditions, storage temperature...
     */
    @Column(name = "note", length = 500)
    private String note;

    /**
     * Line item details (Master - Detail BM2).
     */
    @OneToMany(mappedBy = "importReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ImportReceiptDetail> details = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.importDate == null) {
            this.importDate = LocalDateTime.now();
        }
        if (this.supplierType == null) {
            this.supplierType = "CENTRAL_WAREHOUSE";
        }
        if (this.status == null) {
            this.status = "IMPORTED";
        }
        if (this.totalAmount == null) {
            this.totalAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method to add detail and establish bidirectional association.
     */
    public void addDetail(ImportReceiptDetail detail) {
        if (detail != null) {
            this.details.add(detail);
            detail.setImportReceipt(this);
        }
    }

    /**
     * Recalculate total amount from details list.
     */
    public void recalculateTotalAmount() {
        if (this.details == null || this.details.isEmpty()) {
            this.totalAmount = BigDecimal.ZERO;
            return;
        }
        this.totalAmount = this.details.stream()
                .map(d -> d.getTotalPrice() != null ? d.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
