package com.ute.trasua.service;

import com.ute.trasua.dto.BranchRequest;
import com.ute.trasua.dto.BranchResponse;
import com.ute.trasua.dto.UserResponse;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.repository.EmployeeRepository;
import com.ute.trasua.repository.PosAccountRepository;
import com.ute.trasua.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service handling branch network business logic (UC21).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService {

    private final BranchRepository branchRepository;
    private final EmployeeRepository employeeRepository;
    private final PosAccountRepository posAccountRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll().stream()
                .map(this::enrichToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getAllActiveBranches() {
        return branchRepository.findByActiveTrue().stream()
                .map(this::enrichToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<BranchResponse> searchBranches(String keyword, Boolean active, Pageable pageable) {
        Page<Branch> page = branchRepository.searchBranches(keyword, active, pageable);
        return page.map(this::enrichToResponse);
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {
        Branch branch = getBranchEntityById(id);
        return enrichToResponse(branch);
    }

    @Transactional(readOnly = true)
    public Branch getBranchEntityById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchByCode(String branchCode) {
        Branch branch = branchRepository.findByBranchCode(branchCode)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with code: " + branchCode));
        return enrichToResponse(branch);
    }

    @Transactional(readOnly = true)
    public Optional<BranchResponse> getBranchByManagerUsername(String username) {
        return branchRepository.findFirstByManagerUsername(username)
                .map(this::enrichToResponse);
    }

    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        if (request.getBranchCode() == null || request.getBranchCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Branch code must not be empty!");
        }

        String code = request.getBranchCode().trim().toUpperCase();
        if (branchRepository.existsByBranchCode(code)) {
            throw new IllegalArgumentException("Branch code [" + code + "] already exists in the system!");
        }

        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId()).orElse(null);
        }

        Branch branch = Branch.builder()
                .branchCode(code)
                .name(request.getName().trim())
                .address(request.getAddress().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .hotline(request.getHotline() != null ? request.getHotline().trim() : null)
                .openTime(request.getOpenTime() != null ? request.getOpenTime().trim() : "07:00")
                .closeTime(request.getCloseTime() != null ? request.getCloseTime().trim() : "22:30")
                .active(request.isActive())
                .manager(manager)
                .build();

        Branch saved = branchRepository.save(branch);
        log.info(">>> Created new branch: [{}] {}", saved.getBranchCode(), saved.getName());
        return enrichToResponse(saved);
    }

    @Transactional
    public BranchResponse updateBranch(Long id, BranchRequest request) {
        Branch branch = getBranchEntityById(id);

        if (request.getBranchCode() != null && !request.getBranchCode().trim().isEmpty()) {
            String code = request.getBranchCode().trim().toUpperCase();
            if (branchRepository.existsByBranchCodeAndIdNot(code, id)) {
                throw new IllegalArgumentException("Branch code [" + code + "] is already used by another branch!");
            }
            branch.setBranchCode(code);
        }

        if (request.getName() != null) branch.setName(request.getName().trim());
        if (request.getAddress() != null) branch.setAddress(request.getAddress().trim());
        if (request.getLatitude() != null) branch.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) branch.setLongitude(request.getLongitude());
        if (request.getHotline() != null) branch.setHotline(request.getHotline().trim());
        if (request.getOpenTime() != null) branch.setOpenTime(request.getOpenTime().trim());
        if (request.getCloseTime() != null) branch.setCloseTime(request.getCloseTime().trim());
        branch.setActive(request.isActive());

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId()).orElse(null);
            branch.setManager(manager);
        } else {
            branch.setManager(null);
        }

        Branch updated = branchRepository.save(branch);
        log.info(">>> Updated branch ID {}: [{}] {}", id, updated.getBranchCode(), updated.getName());
        return enrichToResponse(updated);
    }

    @Transactional
    public boolean toggleBranchStatus(Long id) {
        Branch branch = getBranchEntityById(id);
        boolean newStatus = !branch.isActive();
        branch.setActive(newStatus);
        branchRepository.save(branch);
        log.info(">>> Toggled branch [{}] status to: {}", branch.getBranchCode(), newStatus ? "Active" : "Paused");
        return newStatus;
    }

    @Transactional
    public void deleteBranch(Long id) {
        Branch branch = getBranchEntityById(id);
        long countEmp = employeeRepository.countByBranchId(id);
        if (countEmp > 0) {
            // Soft delete / deactivate if still has employees
            branch.setActive(false);
            branchRepository.save(branch);
            log.warn(">>> Branch ID {} still has {} employees -> Deactivated instead of hard delete.", id, countEmp);
        } else {
            branchRepository.delete(branch);
            log.info(">>> Deleted branch ID {} from database.", id);
        }
    }

    @Transactional(readOnly = true)
    public BranchResponse findNearestBranch(Double userLat, Double userLng) {
        List<BranchResponse> activeBranches = getAllActiveBranches();
        if (activeBranches.isEmpty() || userLat == null || userLng == null) {
            return null;
        }

        return activeBranches.stream()
                .filter(b -> b.getLatitude() != null && b.getLongitude() != null)
                .min(Comparator.comparingDouble(b -> b.calculateDistanceKm(userLat, userLng)))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getEligibleManagers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("ROLE_MANAGER") || u.getRoles().contains("ROLE_ADMIN"))
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countActiveBranches() {
        return branchRepository.countByActiveTrue();
    }

    @Transactional(readOnly = true)
    public long countTotalBranches() {
        return branchRepository.count();
    }

    private BranchResponse enrichToResponse(Branch b) {
        long totalEmp = employeeRepository.countByBranchId(b.getId());
        long activeEmp = employeeRepository.countByBranchIdAndStatus(b.getId(), "ACTIVE");
        long totalPos = posAccountRepository.countByBranchId(b.getId());
        return BranchResponse.fromEntity(b, totalEmp, activeEmp, totalPos);
    }
}
