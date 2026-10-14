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

    void resendDriverCredentials(Long driverId);

    Map<String, Object> getBranchDriverStatistics(Long branchId);
}
