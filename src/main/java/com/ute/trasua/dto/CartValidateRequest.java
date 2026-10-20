package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request payload sent by frontend client to validate items and prices against server DB.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartValidateRequest {

    @Builder.Default
    private List<CartItemDto> items = new ArrayList<>();

    private String voucherCode;

    private Long branchId;
}
