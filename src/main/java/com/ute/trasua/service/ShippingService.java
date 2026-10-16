package com.ute.trasua.service;

import com.ute.trasua.dto.shipping.ShippingCalculateRequest;
import com.ute.trasua.dto.shipping.ShippingCalculateResponse;
import com.ute.trasua.entity.Branch;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Haversine distance calculation, shipping fee engine,
 * and smart branch routing (Module 3 - UC05).
 */
public interface ShippingService {

    /**
     * Calculates great-circle distance in kilometers between two GPS coordinates using Haversine formula.
     *
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers rounded to 2 decimal places
     */
    double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2);

    /**
     * Calculates shipping fee based on Haversine distance policy:
     * - distance <= 2.0 km: 0 VND (Free ship)
     * - distance > 2.0 km: (distance - 2.0) * 5,000 VND / km (rounded up to thousands)
     *
     * @param distanceKm Distance in kilometers
     * @return Calculated shipping fee
     */
    BigDecimal calculateShippingFee(double distanceKm);

    /**
     * Finds the nearest active branch to customer's GPS coordinates.
     *
     * @param customerLat Customer latitude
     * @param customerLng Customer longitude
     * @param activeBranches List of currently active branches
     * @return The nearest Branch entity
     */
    Branch findNearestBranch(double customerLat, double customerLng, List<Branch> activeBranches);

    /**
     * Orchestrates shipping calculation, branch routing, and response building.
     *
     * @param request Input coordinates and optional target branch
     * @return Full calculation response with distance, fee, branch, and user-facing message
     */
    ShippingCalculateResponse calculateAndRoute(ShippingCalculateRequest request);
}
