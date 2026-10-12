package com.ute.trasua.dto.pos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosCashCheckoutRequest {

    private PosOrderRequest order;
    private BigDecimal cashTendered;
    private BigDecimal changeAmount;
}
