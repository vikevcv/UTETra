package com.ute.trasua.service;

import com.ute.trasua.dto.UserResponse;
import com.ute.trasua.dto.UserSearchRequest;
import com.ute.trasua.dto.UserUpdateRequest;
import com.ute.trasua.dto.user.UserDriverTripDto;
import com.ute.trasua.dto.user.UserOrderHistoryDto;
import org.springframework.data.domain.Page;

import java.util.List;
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

    /**
     * Lấy toàn bộ lịch sử các đơn hàng mà người dùng đã đặt mua.
     */
    List<UserOrderHistoryDto> getUserOrderHistory(Long userId);

    /**
     * Lấy thống kê tổng quan lịch sử đơn hàng của người dùng.
     */
    Map<String, Object> getUserOrderSummary(Long userId);

    /**
     * Lấy toàn bộ lịch sử các cuốc giao hàng của Shipper (nếu người dùng là tài xế).
     */
    List<UserDriverTripDto> getDriverTripHistory(Long userId);

    /**
     * Lấy thống kê tổng quan hoạt động giao hàng của Shipper.
     */
    Map<String, Object> getDriverTripSummary(Long userId);
}
