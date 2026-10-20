package com.ute.trasua.service;

import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.User;

public interface OrderSecurityService {

    /**
     * Kiểm tra người dùng có quyền truy cập thông tin đơn hàng này hay không.
     * Cho phép nếu:
     * 1. Khách hàng là chủ sở hữu đơn hàng (order.getUser() == currentUser hoặc số điện thoại khớp)
     * 2. Quản trị viên, Quản lý, Nhân viên chi nhánh (ROLE_ADMIN, ROLE_MANAGER, ROLE_STAFF, ROLE_POS)
     * 3. Tài xế được phân công phụ trách đơn hàng này (ROLE_DRIVER && order.getDriver() == currentUser)
     *
     * @param order Thực thể đơn hàng
     * @param currentUser Người dùng đang đăng nhập
     * @return true nếu hợp lệ, false nếu không có quyền
     */
    boolean canAccessOrder(Order order, User currentUser);

    /**
     * Kiểm tra quyền truy cập theo mã đơn hàng chuỗi.
     */
    boolean canAccessOrder(String orderCode, User currentUser);
}
