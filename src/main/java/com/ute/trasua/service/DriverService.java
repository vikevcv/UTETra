package com.ute.trasua.service;

import com.ute.trasua.dto.CreateDriverRequest;
import com.ute.trasua.dto.DriverResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface DriverService {

    Page<DriverResponse> searchDriversByBranch(Long branchId, String keyword, String workingStatus, Pageable pageable);

    DriverResponse createDriver(CreateDriverRequest request, Long branchId);

    DriverResponse updateDriver(Long driverId, com.ute.trasua.dto.UpdateDriverRequest request);

    DriverResponse toggleDriverLock(Long driverId, boolean locked, String reason);

    DriverResponse updateWorkingStatus(Long driverId, String newStatus);

    String resendDriverCredentials(Long driverId);

    Map<String, Object> getBranchDriverStatistics(Long branchId);

    Map<String, Object> getAllDriverStatistics();

    com.ute.trasua.dto.DriverResponse toggleDriverAvailability(Long driverId, Boolean isAvailable);

    com.ute.trasua.dto.handover.HandoverOrderDto reportForceMajeureIncident(Long orderId, Long driverId, String incidentReason, String note);

    com.ute.trasua.entity.Driver resolveCurrentDriver(org.springframework.security.core.Authentication authentication, Long requestedDriverId);

    com.ute.trasua.entity.Order seedSampleDeliveryOrder(com.ute.trasua.entity.Driver driver, String fallbackUsername);
}
