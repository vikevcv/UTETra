package com.ute.trasua.service.impl;

import com.ute.trasua.dto.shipping.ShippingCalculateRequest;
import com.ute.trasua.dto.shipping.ShippingCalculateResponse;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of ShippingService for Haversine distance, shipping fees, and branch routing (UC05).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingServiceImpl implements ShippingService {

    private final BranchRepository branchRepository;

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double FREE_SHIP_THRESHOLD_KM = 2.0;
    private static final long SURCHARGE_PER_KM = 5000L;

    @Override
    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        return Math.round(distance * 100.0) / 100.0;
    }

    @Override
    public BigDecimal calculateShippingFee(double distanceKm) {
        if (distanceKm <= FREE_SHIP_THRESHOLD_KM) {
            return BigDecimal.ZERO;
        }

        double extraKm = distanceKm - FREE_SHIP_THRESHOLD_KM;
        long fee = (long) Math.ceil(extraKm) * SURCHARGE_PER_KM;
        return BigDecimal.valueOf(fee);
    }

    @Override
    public Branch findNearestBranch(double customerLat, double customerLng, List<Branch> activeBranches) {
        if (activeBranches == null || activeBranches.isEmpty()) {
            return null;
        }

        return activeBranches.stream()
                .filter(b -> b.getLatitude() != null && b.getLongitude() != null)
                .min(Comparator.comparingDouble(b -> calculateDistanceKm(customerLat, customerLng, b.getLatitude(), b.getLongitude())))
                .orElse(activeBranches.get(0));
    }

    @Override
    public ShippingCalculateResponse calculateAndRoute(ShippingCalculateRequest request) {
        List<Branch> activeBranches = branchRepository.findByActiveTrue();
        if (activeBranches.isEmpty()) {
            throw new IllegalStateException("Hiện tại không có chi nhánh nào đang hoạt động.");
        }

        Branch selectedBranch = null;
        if (request.getBranchId() != null) {
            selectedBranch = branchRepository.findById(request.getBranchId())
                    .filter(Branch::isActive)
                    .orElse(activeBranches.get(0));
        } else if (request.getLatitude() != null && request.getLongitude() != null) {
            selectedBranch = findNearestBranch(request.getLatitude(), request.getLongitude(), activeBranches);
        } else {
            selectedBranch = activeBranches.get(0);
        }

        double distanceKm = 0.0;
        BigDecimal fee = BigDecimal.ZERO;
        boolean freeShip = true;

        if (request.getLatitude() != null && request.getLongitude() != null &&
                selectedBranch.getLatitude() != null && selectedBranch.getLongitude() != null) {

            distanceKm = calculateDistanceKm(
                    selectedBranch.getLatitude(), selectedBranch.getLongitude(),
                    request.getLatitude(), request.getLongitude()
            );
            fee = calculateShippingFee(distanceKm);
            freeShip = fee.compareTo(BigDecimal.ZERO) == 0;
        }

        String msg = freeShip
                ? String.format("Miễn phí vận chuyển (Khoảng cách %.1f km <= %.1f km)", distanceKm, FREE_SHIP_THRESHOLD_KM)
                : String.format("Phụ phí vận chuyển: %s đ (Khoảng cách %.1f km)", fee, distanceKm);

        return ShippingCalculateResponse.builder()
                .branchId(selectedBranch.getId())
                .branchName(selectedBranch.getName())
                .branchAddress(selectedBranch.getAddress())
                .branchLatitude(selectedBranch.getLatitude())
                .branchLongitude(selectedBranch.getLongitude())
                .branchHotline(selectedBranch.getHotline())
                .customerLatitude(request.getLatitude())
                .customerLongitude(request.getLongitude())
                .customerAddress(request.getAddress())
                .distanceKm(distanceKm)
                .shippingFee(fee)
                .freeShip(freeShip)
                .message(msg)
                .build();
    }
}
