package com.ute.trasua.service;

import com.ute.trasua.dto.UserResponse;
import com.ute.trasua.dto.UserSearchRequest;
import com.ute.trasua.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * Service interface xử lý nghiệp vụ quản lý người dùng và giám sát tài xế toàn chuỗi (Module 11 - UC20, UC25).
 */
public interface UserManagementService {

    Page<UserResponse> searchUsers(UserSearchRequest request);

    UserResponse getUserById(Long id);

    void toggleLock(Long id, boolean lock, String reason);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    Page<UserResponse> searchDrivers(String keyword, int page, int size);

    Map<String, Object> getUserStatistics();
}
