package com.ute.trasua.service;

import com.ute.trasua.dto.BranchRequest;
import com.ute.trasua.dto.BranchResponse;
import com.ute.trasua.dto.UserResponse;
import com.ute.trasua.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface handling branch network business logic (UC21).
 */
public interface BranchService {

    List<BranchResponse> getAllBranches();

    List<BranchResponse> getAllActiveBranches();

    Page<BranchResponse> searchBranches(String keyword, Boolean active, Pageable pageable);

    BranchResponse getBranchById(Long id);

    Branch getBranchEntityById(Long id);

    BranchResponse getBranchByCode(String branchCode);

    Optional<BranchResponse> getBranchByManagerUsername(String username);

    BranchResponse createBranch(BranchRequest request);

    BranchResponse updateBranch(Long id, BranchRequest request);

    boolean toggleBranchStatus(Long id);

    void deleteBranch(Long id);

    BranchResponse findNearestBranch(Double userLat, Double userLng);

    List<UserResponse> getEligibleManagers();

    long countActiveBranches();

    long countTotalBranches();
}
