package com.ute.trasua.controller.api;

import com.ute.trasua.dto.shipping.ShippingCalculateRequest;
import com.ute.trasua.dto.shipping.ShippingCalculateResponse;
import com.ute.trasua.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for distance and shipping fee calculations using Haversine formula (Module 3 - UC05).
 */
@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
@Slf4j
public class ShippingApiController {

    private final ShippingService shippingService;

    /**
     * Calculates great-circle Haversine distance and shipping surcharge in real time.
     */
    @PostMapping("/calculate")
    public ResponseEntity<ShippingCalculateResponse> calculateShipping(@RequestBody ShippingCalculateRequest request) {
        ShippingCalculateResponse response = shippingService.calculateAndRoute(request);
        return ResponseEntity.ok(response);
    }
}
