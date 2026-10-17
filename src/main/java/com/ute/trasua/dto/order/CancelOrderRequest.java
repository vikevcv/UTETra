package com.ute.trasua.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Request payload for customer cancelling an online order (UC28).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reason;
    private String customerPhone;
}
