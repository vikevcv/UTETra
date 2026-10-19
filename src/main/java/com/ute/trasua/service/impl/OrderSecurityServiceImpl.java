package com.ute.trasua.service.impl;

import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.service.OrderSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSecurityServiceImpl implements OrderSecurityService {

    private final OrderRepository orderRepository;

    @Override
    public boolean canAccessOrder(Order order, User currentUser) {
        if (order == null || currentUser == null) {
            return false;
        }

        // 1. Quản trị viên, Quản lý, Nhân viên chi nhánh (Toàn quyền tra cứu vận hành)
        if (currentUser.getRoles() != null) {
            boolean isStaffOrAdmin = currentUser.getRoles().stream().anyMatch(r ->
                    "ROLE_ADMIN".equalsIgnoreCase(r) || "ADMIN".equalsIgnoreCase(r)
                            || "ROLE_MANAGER".equalsIgnoreCase(r) || "MANAGER".equalsIgnoreCase(r)
                            || "ROLE_STAFF".equalsIgnoreCase(r) || "STAFF".equalsIgnoreCase(r)
                            || "ROLE_POS".equalsIgnoreCase(r));
            if (isStaffOrAdmin) {
                return true;
            }
        }

        // 2. Tài xế được phân công giao đơn này
        if (order.getDriver() != null && order.getDriver().getUser() != null) {
            if (order.getDriver().getUser().getId().equals(currentUser.getId())) {
                return true;
            }
        }

        // 3. Khách hàng là chủ sở hữu đơn hàng (Owner)
        if (order.getUser() != null && order.getUser().getId().equals(currentUser.getId())) {
            return true;
        }

        // 4. Trường hợp đối chiếu số điện thoại khách hàng (đối với các đơn cũ)
        if (order.getUser() == null && order.getCustomerPhone() != null && currentUser.getPhone() != null) {
            String orderPhone = order.getCustomerPhone().replaceAll("[^0-9]", "");
            String userPhone = currentUser.getPhone().replaceAll("[^0-9]", "");
            if (!orderPhone.isEmpty() && orderPhone.equals(userPhone)) {
                return true;
            }
        }

        log.warn(">>> [SECURITY ACCESS DENIED] User '{}' (id={}) cố tình truy cập đơn hàng '{}' thuộc sở hữu của người dùng khác!",
                currentUser.getUsername(), currentUser.getId(), order.getOrderCode());
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAccessOrder(String orderCode, User currentUser) {
        if (orderCode == null || orderCode.isBlank() || currentUser == null) {
            return false;
        }
        Order order = orderRepository.findByOrderCode(orderCode.trim().toUpperCase()).orElse(null);
        return canAccessOrder(order, currentUser);
    }
}
