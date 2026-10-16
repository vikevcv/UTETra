package com.ute.trasua.dto.shipping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for calculating shipping distance and fee (Module 3 - UC05).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingCalculateRequest {

    private Long branchId;
    private Double latitude;
    private Double longitude;
    private String address;
}
