package com.ute.trasua.dto.shipping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO response with Haversine distance and shipping fee details (Module 3 - UC05).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingCalculateResponse {

    private Long branchId;
    private String branchName;
    private String branchAddress;
    private Double branchLatitude;
    private Double branchLongitude;
    private String branchHotline;

    private Double customerLatitude;
    private Double customerLongitude;
    private String customerAddress;

    private Double distanceKm;
    private BigDecimal shippingFee;
    private boolean freeShip;
    private String message;
}
