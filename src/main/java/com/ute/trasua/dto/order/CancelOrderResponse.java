package com.ute.trasua.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Response payload after successfully cancelling an order and issuing an 80% compensation voucher (UC28).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String orderCode;
    private String status;
    private String compensationVoucherCode;
    private BigDecimal compensationAmount;
    private String expiryDateFormatted;
    private boolean restoredOldVoucher;
    private String restoredVoucherCode;
    private String message;
}
