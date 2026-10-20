package com.ute.trasua.service.impl;

import com.ute.trasua.dto.UserResponse;
import com.ute.trasua.dto.UserSearchRequest;
import com.ute.trasua.dto.UserUpdateRequest;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Service implementation xử lý nghiệp vụ quản lý người dùng và giám sát tài xế toàn chuỗi (Module 11 - UC20, UC25).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final com.ute.trasua.repository.BranchRepository branchRepository;
    private final com.ute.trasua.repository.PosAccountRepository posAccountRepository;
    private final com.ute.trasua.repository.DriverRepository driverRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(UserSearchRequest request) {
        int page = Math.max(0, request.getPage());
        int size = request.getSize() > 0 ? request.getSize() : 10;
        
        Sort.Direction direction = "asc".equalsIgnoreCase(request.getSortDir()) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        
        String sortBy = (request.getSortBy() != null && !request.getSortBy().isBlank()) 
                ? request.getSortBy() 
                : "id";
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        String keyword = (request.getKeyword() != null) ? request.getKeyword().trim() : null;
        String role = (request.getRole() != null && !request.getRole().isBlank()) ? request.getRole().trim() : null;
        Boolean locked = request.getLocked();

        Page<User> usersPage = userRepository.searchUsers(keyword, role, locked, pageable);
        return usersPage.map(UserResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public void toggleLock(Long id, boolean lock, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));

        // Bảo vệ: Không cho phép khóa tài khoản ADMIN tối cao
        if (lock && user.getRoles().contains("ROLE_ADMIN")) {
            throw new IllegalStateException("Không được phép khóa tài khoản Quản trị viên (ADMIN) cấp cao!");
        }

        user.setLocked(lock);
        if (lock) {
            user.setLockReason(reason != null && !reason.isBlank() 
                    ? reason 
                    : "Tài khoản bị khóa bởi Quản trị viên");
            log.info("User {} (ID: {}) bị khóa với lý do: {}", user.getUsername(), id, user.getLockReason());
        } else {
            user.setLockReason(null);
            log.info("User {} (ID: {}) đã được mở khóa", user.getUsername(), id);
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar().trim());
        }

        // Cập nhật vai trò (roles) và chi nhánh trực thuộc
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<String> newRoles = new HashSet<>(request.getRoles());
            boolean requiresBranch = newRoles.contains("ROLE_MANAGER") 
                    || newRoles.contains("ROLE_STAFF") 
                    || newRoles.contains("ROLE_DRIVER");

            if (requiresBranch) {
                Long branchId = request.getBranchId();
                if (branchId == null) {
                    throw new IllegalArgumentException("Vui lòng chỉ định Chi nhánh trực thuộc khi phân quyền Quản lý, Nhân viên hoặc Tài xế!");
                }
                com.ute.trasua.entity.Branch branch = branchRepository.findById(branchId)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh với ID: " + branchId));

                user.setRoles(newRoles);
                user.setBranch(branch);

                if (newRoles.contains("ROLE_MANAGER")) {
                    // Gỡ quyền manager ở các chi nhánh cũ khác
                    java.util.List<com.ute.trasua.entity.Branch> oldBranches = branchRepository.findByManager(user);
                    for (com.ute.trasua.entity.Branch oldB : oldBranches) {
                        if (!oldB.getId().equals(branch.getId())) {
                            oldB.setManager(null);
                            branchRepository.save(oldB);
                        }
                    }
                    branch.setManager(user);
                    branchRepository.save(branch);
                } else {
                    // Nếu không còn là manager, gỡ khỏi branch.manager nếu có
                    java.util.List<com.ute.trasua.entity.Branch> managedBranches = branchRepository.findByManager(user);
                    for (com.ute.trasua.entity.Branch b : managedBranches) {
                        b.setManager(null);
                        branchRepository.save(b);
                    }
                }

                if (newRoles.contains("ROLE_STAFF")) {
                    com.ute.trasua.entity.PosAccount posAcc = posAccountRepository.findByUserId(user.getId())
                            .or(() -> posAccountRepository.findByUsername(user.getUsername()))
                            .orElse(null);
                    if (posAcc != null) {
                        posAcc.setBranch(branch);
                        posAcc.setUser(user);
                        posAcc.setActive(true);
                        posAccountRepository.save(posAcc);
                    } else {
                        com.ute.trasua.entity.PosAccount newPos = com.ute.trasua.entity.PosAccount.builder()
                                .username(user.getUsername())
                                .counterName(user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : (user.getUsername() + " (Quầy POS)"))
                                .password(user.getPassword() != null ? user.getPassword() : "123456")
                                .branch(branch)
                                .user(user)
                                .active(true)
                                .note("Phân quyền nhân viên chi nhánh " + branch.getName())
                                .build();
                        posAccountRepository.save(newPos);
                    }
                }

                if (newRoles.contains("ROLE_DRIVER")) {
                    com.ute.trasua.entity.Driver driver = driverRepository.findByUserId(user.getId()).orElse(null);
                    if (driver != null) {
                        driver.setBranch(branch);
                        driverRepository.save(driver);
                    } else {
                        String plate = "59-T1 " + (10000 + (int) (Math.random() * 89999));
                        com.ute.trasua.entity.Driver newDriver = com.ute.trasua.entity.Driver.builder()
                                .user(user)
                                .branch(branch)
                                .licensePlate(plate)
                                .vehicleType("MOTORBIKE")
                                .workingStatus("ONLINE")
                                .baseSalary(new java.math.BigDecimal("5000000"))
                                .kpiScore(5.0)
                                .completedOrders(0)
                                .build();
                        driverRepository.save(newDriver);
                    }
                }
            } else {
                // Khách hàng hoặc Admin toàn chuỗi: không gán chi nhánh cố định
                user.setRoles(newRoles);
                user.setBranch(null);
                java.util.List<com.ute.trasua.entity.Branch> managedBranches = branchRepository.findByManager(user);
                for (com.ute.trasua.entity.Branch b : managedBranches) {
                    b.setManager(null);
                    branchRepository.save(b);
                }
            }
        } else if (request.getBranchId() != null) {
            // Trường hợp không đổi role nhưng cập nhật chi nhánh
            com.ute.trasua.entity.Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh với ID: " + request.getBranchId()));
            user.setBranch(branch);
            if (user.getRoles() != null && user.getRoles().contains("ROLE_MANAGER")) {
                java.util.List<com.ute.trasua.entity.Branch> oldBranches = branchRepository.findByManager(user);
                for (com.ute.trasua.entity.Branch oldB : oldBranches) {
                    if (!oldB.getId().equals(branch.getId())) {
                        oldB.setManager(null);
                        branchRepository.save(oldB);
                    }
                }
                branch.setManager(user);
                branchRepository.save(branch);
            }
        }

        // Cập nhật trạng thái khóa nếu có thay đổi
        if (request.getLocked() != null && request.getLocked() != user.isLocked()) {
            toggleLock(id, request.getLocked(), request.getLockReason());
        } else {
            userRepository.save(user);
        }

        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchDrivers(String keyword, int page, int size) {
        int pageIndex = Math.max(0, page);
        int pageSize = size > 0 ? size : 10;
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        
        String cleanKeyword = (keyword != null) ? keyword.trim() : null;
        Page<User> driverPage = userRepository.searchDrivers(cleanKeyword, pageable);
        return driverPage.map(UserResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics() {
        long totalUsers = userRepository.count();
        long lockedUsers = userRepository.countByLocked(true);
        long activeUsers = totalUsers - lockedUsers;
        long totalDrivers = userRepository.countByRole("ROLE_DRIVER");
        long totalCustomers = userRepository.countByRole("ROLE_CUSTOMER");
        long totalStaff = userRepository.countByRole("ROLE_STAFF") + userRepository.countByRole("ROLE_MANAGER");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("lockedUsers", lockedUsers);
        stats.put("totalDrivers", totalDrivers);
        stats.put("totalCustomers", totalCustomers);
        stats.put("totalStaff", totalStaff);
        return stats;
    }
}
