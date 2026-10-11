package com.ute.trasua.service;

import com.ute.trasua.dto.UserResponse;
import com.ute.trasua.dto.UserSearchRequest;
import com.ute.trasua.dto.UserUpdateRequest;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
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

/**
 * Service xử lý nghiệp vụ quản lý người dùng và giám sát tài xế toàn chuỗi (Module 11 - UC20, UC25).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UserRepository userRepository;

    /**
     * Tìm kiếm và phân trang người dùng theo tiêu chí lọc động.
     */
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

    /**
     * Lấy thông tin chi tiết một người dùng theo ID.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));
        return UserResponse.fromEntity(user);
    }

    /**
     * Khóa hoặc mở khóa tài khoản người dùng (UC20).
     */
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

    /**
     * Cập nhật thông tin tài khoản người dùng (Họ tên, SĐT, Phân quyền vai trò).
     */
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

        // Cập nhật vai trò (roles) nếu được chỉ định
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(new HashSet<>(request.getRoles()));
        }

        // Cập nhật trạng thái khóa nếu có thay đổi
        if (request.getLocked() != null && request.getLocked() != user.isLocked()) {
            toggleLock(id, request.getLocked(), request.getLockReason());
        } else {
            userRepository.save(user);
        }

        return UserResponse.fromEntity(user);
    }

    /**
     * Tìm kiếm và giám sát danh sách tài xế (ROLE_DRIVER) toàn chuỗi (Module 11 - UC25).
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> searchDrivers(String keyword, int page, int size) {
        int pageIndex = Math.max(0, page);
        int pageSize = size > 0 ? size : 10;
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        
        String cleanKeyword = (keyword != null) ? keyword.trim() : null;
        Page<User> driverPage = userRepository.searchDrivers(cleanKeyword, pageable);
        return driverPage.map(UserResponse::fromEntity);
    }

    /**
     * Thống kê tổng hợp số liệu người dùng phục vụ Dashboard KPI.
     */
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
