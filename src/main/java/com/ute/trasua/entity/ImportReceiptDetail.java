package com.ute.trasua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing line items in an ingredient import receipt (Detail BM2 - Task 4.1 / Module 14).
 * Table: import_receipt_details (Reference Table 3.20 in report).
 */
@Entity
@Table(name = "import_receipt_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportReceiptDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_receipt_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ImportReceipt importReceipt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    /**
     * Quantity imported (> 0).
     */
    @Column(name = "quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    /**
     * Unit purchase price on invoice (> 0) (VNĐ).
     */
    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Line total = quantity * unitPrice (VNĐ).
     */
    @Column(name = "total_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalPrice;

    /**
     * Expiry date printed on packaging (Food safety compliance, expiryDate > importDate).
     */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "note", length = 255)
    private String note;

    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        if (this.quantity != null && this.unitPrice != null) {
            this.totalPrice = this.quantity.multiply(this.unitPrice);
        } else if (this.totalPrice == null) {
            this.totalPrice = BigDecimal.ZERO;
        }
    }
}
