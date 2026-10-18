package com.ute.trasua.service.impl;

import com.ute.trasua.dto.BranchRequest;
import com.ute.trasua.dto.BranchResponse;
import com.ute.trasua.dto.UserResponse;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.repository.EmployeeRepository;
import com.ute.trasua.repository.PosAccountRepository;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.BranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation handling branch network business logic (UC21).
 */
@Service
@Slf4j
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final EmployeeRepository employeeRepository;
    private final PosAccountRepository posAccountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public BranchServiceImpl(
            BranchRepository branchRepository,
            EmployeeRepository employeeRepository,
            PosAccountRepository posAccountRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.branchRepository = branchRepository;
        this.employeeRepository = employeeRepository;
        this.posAccountRepository = posAccountRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll().stream()
                .map(this::enrichToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getAllActiveBranches() {
        return branchRepository.findByActiveTrue().stream()
                .map(this::enrichToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Branch> getAllBranchEntities() {
        return branchRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Branch> getActiveBranchEntities() {
        return branchRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BranchResponse> searchBranches(String keyword, Boolean active, Pageable pageable) {
        Page<Branch> page = branchRepository.searchBranches(keyword, active, pageable);
        return page.map(this::enrichToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {
        Branch branch = getBranchEntityById(id);
        return enrichToResponse(branch);
    }

    @Override
    @Transactional(readOnly = true)
    public Branch getBranchEntityById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Branch> findBranchEntityById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return branchRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Branch getDefaultBranch() {
        return branchRepository.findByActiveTrue().stream().findFirst()
                .or(() -> branchRepository.findAll().stream().findFirst())
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getBranchByCode(String branchCode) {
        Branch branch = branchRepository.findByBranchCode(branchCode)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with code: " + branchCode));
        return enrichToResponse(branch);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BranchResponse> getBranchByManagerUsername(String username) {
        return branchRepository.findFirstByManagerUsername(username)
                .map(this::enrichToResponse);
    }

    @Override
    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        if (request.getBranchCode() == null || request.getBranchCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã chi nhánh không được để trống!");
        }

        String code = request.getBranchCode().trim().toUpperCase();
        if (branchRepository.existsByBranchCode(code)) {
            throw new IllegalArgumentException("Mã chi nhánh [" + code + "] đã tồn tại trong hệ thống!");
        }

        // Tự động cấp tài khoản hoặc liên kết quản lý
        ManagerProvisionResult mgrResult = resolveManager(request, code, request.getName() != null ? request.getName().trim() : code);

        Branch branch = Branch.builder()
                .branchCode(code)
                .name(request.getName() != null ? request.getName().trim() : "")
                .address(request.getAddress() != null ? request.getAddress().trim() : "")
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .hotline(request.getHotline() != null ? request.getHotline().trim() : null)
                .openTime(request.getOpenTime() != null ? request.getOpenTime().trim() : "07:00")
                .closeTime(request.getCloseTime() != null ? request.getCloseTime().trim() : "22:30")
                .active(request.isActive())
                .manager(mgrResult.user())
                .build();

        Branch saved = branchRepository.save(branch);
        log.info(">>> Created new branch: [{}] {}", saved.getBranchCode(), saved.getName());

        BranchResponse resp = enrichToResponse(saved);
        if (mgrResult.created()) {
            resp.setGeneratedManagerUsername(mgrResult.username());
            resp.setGeneratedManagerPassword(mgrResult.rawPassword());
            resp.setManagerAccountCreated(true);
        }
        return resp;
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(Long id, BranchRequest request) {
        Branch branch = getBranchEntityById(id);

        if (request.getBranchCode() != null && !request.getBranchCode().trim().isEmpty()) {
            String code = request.getBranchCode().trim().toUpperCase();
            if (branchRepository.existsByBranchCodeAndIdNot(code, id)) {
                throw new IllegalArgumentException("Mã chi nhánh [" + code + "] đã được sử dụng bởi chi nhánh khác!");
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

        // Cập nhật người quản lý (Tạo mới hoặc Điều chuyển quản lý sẵn có)
        if ("NEW".equalsIgnoreCase(request.getManagerOption()) && hasNewManagerInfo(request)) {
            ManagerProvisionResult mgrResult = resolveManager(request, branch.getBranchCode(), branch.getName());
            branch.setManager(mgrResult.user());
        } else if ("EXISTING".equalsIgnoreCase(request.getManagerOption())) {
            if (request.getManagerId() != null) {
                User manager = userRepository.findById(request.getManagerId()).orElse(null);
                branch.setManager(manager);
            } else {
                branch.setManager(null);
            }
        } else if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId()).orElse(null);
            branch.setManager(manager);
        }

        Branch updated = branchRepository.save(branch);
        log.info(">>> Updated branch ID {}: [{}] {}", id, updated.getBranchCode(), updated.getName());
        return enrichToResponse(updated);
    }

    /**
     * Helper kiểm tra xem request có thông tin quản lý mới cần cấp tài khoản hay không.
     */
    private boolean hasNewManagerInfo(BranchRequest request) {
        return (request.getNewManagerFullName() != null && !request.getNewManagerFullName().trim().isEmpty())
                || (request.getNewManagerPhone() != null && !request.getNewManagerPhone().trim().isEmpty())
                || (request.getNewManagerEmail() != null && !request.getNewManagerEmail().trim().isEmpty())
                || (request.getNewManagerUsername() != null && !request.getNewManagerUsername().trim().isEmpty());
    }

    /**
     * Tự động khởi tạo hoặc chỉ định tài khoản Quản lý chi nhánh (ROLE_MANAGER).
     */
    private ManagerProvisionResult resolveManager(BranchRequest request, String branchCode, String branchName) {
        String option = request.getManagerOption();
        boolean isExisting = "EXISTING".equalsIgnoreCase(option);

        // Trường hợp 1: Chọn quản lý có sẵn từ danh sách
        if (isExisting && request.getManagerId() != null) {
            User existing = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quản lý ID: " + request.getManagerId()));
            return new ManagerProvisionResult(existing, null, null, false);
        }

        // Trường hợp 2: Tự nhập quản lý mới và tự động cấp tài khoản
        boolean hasInfo = hasNewManagerInfo(request);
        if (!isExisting || hasInfo) {
            // Xác định Tên đăng nhập (username)
            String username;
            if (request.getNewManagerUsername() != null && !request.getNewManagerUsername().trim().isEmpty()) {
                username = request.getNewManagerUsername().trim().toLowerCase();
                if (userRepository.existsByUsername(username)) {
                    throw new IllegalArgumentException("Tên đăng nhập quản lý [" + username + "] đã tồn tại trong hệ thống!");
                }
            } else {
                String sanitizedCode = branchCode.toLowerCase().replaceAll("[^a-z0-9]", "");
                String baseUsername = "ql_" + (sanitizedCode.isEmpty() ? "branch" : sanitizedCode);
                username = baseUsername;
                int suffix = 1;
                while (userRepository.existsByUsername(username)) {
                    username = baseUsername + "_" + suffix++;
                }
            }

            // Xác định Mật khẩu khởi tạo
            String rawPassword;
            if (request.getNewManagerPassword() != null && !request.getNewManagerPassword().trim().isEmpty()) {
                rawPassword = request.getNewManagerPassword().trim();
            } else {
                rawPassword = "Branch@123";
            }

            // Xác định Email
            String email;
            if (request.getNewManagerEmail() != null && !request.getNewManagerEmail().trim().isEmpty()) {
                email = request.getNewManagerEmail().trim().toLowerCase();
                if (userRepository.existsByEmail(email)) {
                    throw new IllegalArgumentException("Email quản lý [" + email + "] đã được sử dụng bởi tài khoản khác!");
                }
            } else {
                String baseEmail = username + "@utetra.vn";
                email = baseEmail;
                int suffix = 1;
                while (userRepository.existsByEmail(email)) {
                    email = username + suffix++ + "@utetra.vn";
                }
            }

            // Họ và tên
            String fullName = (request.getNewManagerFullName() != null && !request.getNewManagerFullName().trim().isEmpty())
                    ? request.getNewManagerFullName().trim()
                    : "Quản lý " + branchName;

            // Số điện thoại
            String phone = (request.getNewManagerPhone() != null && !request.getNewManagerPhone().trim().isEmpty())
                    ? request.getNewManagerPhone().trim()
                    : (request.getHotline() != null ? request.getHotline().trim() : null);

            // Khởi tạo thực thể User với vai trò ROLE_MANAGER
            User newManager = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(rawPassword))
                    .fullName(fullName)
                    .phone(phone)
                    .email(email)
                    .enabled(true)
                    .locked(false)
                    .roles(Set.of("ROLE_MANAGER"))
                    .build();

            User savedUser = userRepository.save(newManager);
            log.info(">>> Auto-provisioned new Manager account: @{} ({}) for branch [{}]", username, email, branchCode);
            return new ManagerProvisionResult(savedUser, username, rawPassword, true);
        }

        // Trường hợp 3: Có managerId dự phòng
        if (request.getManagerId() != null) {
            User existing = userRepository.findById(request.getManagerId()).orElse(null);
            return new ManagerProvisionResult(existing, null, null, false);
        }

        return new ManagerProvisionResult(null, null, null, false);
    }

    private record ManagerProvisionResult(User user, String username, String rawPassword, boolean created) {}

    @Override
    @Transactional
    public boolean toggleBranchStatus(Long id) {
        Branch branch = getBranchEntityById(id);
        boolean newStatus = !branch.isActive();
        branch.setActive(newStatus);
        branchRepository.save(branch);
        log.info(">>> Toggled branch [{}] status to: {}", branch.getBranchCode(), newStatus ? "Active" : "Paused");
        return newStatus;
    }

    @Override
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

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getEligibleManagers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("ROLE_MANAGER") || u.getRoles().contains("ROLE_ADMIN"))
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveBranches() {
        return branchRepository.countByActiveTrue();
    }

    @Override
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
