package com.ute.trasua.controller.api;

import com.ute.trasua.dto.order.CancelOrderRequest;
import com.ute.trasua.dto.order.CancelOrderResponse;
import com.ute.trasua.dto.order.OrderLookupRequest;
import com.ute.trasua.dto.order.OrderTrackingDto;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.OnlineOrderService;
import com.ute.trasua.service.OrderTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for order tracking data, phone lookups, and order cancellation (Module 3 - UC05, UC28).
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderTrackingApiController {

    private final OrderTrackingService orderTrackingService;
    private final OnlineOrderService onlineOrderService;
    private final UserRepository userRepository;
    private final com.ute.trasua.service.OrderSecurityService orderSecurityService;

    /**
     * Gets real-time tracking data for a specific order.
     */
    @GetMapping("/tracking/{orderCode}")
    public ResponseEntity<?> getTrackingData(@PathVariable("orderCode") String orderCode, Authentication authentication) {
        User currentUser = resolveCurrentUser(authentication);
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Vui lòng đăng nhập để xem thông tin đơn hàng."
            ));
        }

        if (!orderSecurityService.canAccessOrder(orderCode, currentUser)) {
            log.warn(">>> [SECURITY IDOR BLOCK] User '{}' cố tình gọi API tracking đơn '{}' không thuộc quyền sở hữu",
                    currentUser.getUsername(), orderCode);
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Bạn không có quyền xem thông tin đơn hàng này."
            ));
        }

        OrderTrackingDto dto = orderTrackingService.getTrackingDetails(orderCode);
        return ResponseEntity.ok(dto);
    }

    /**
     * Hủy đơn hàng trực tuyến khi chưa bắt đầu pha chế & nhận mã voucher bồi hoàn 80% (UC28).
     */
    @PostMapping("/tracking/{orderCode}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable("orderCode") String orderCode,
            @RequestBody(required = false) CancelOrderRequest request,
            Authentication authentication) {
        User currentUser = resolveCurrentUser(authentication);
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Vui lòng đăng nhập để hủy đơn hàng."
            ));
        }

        if (!orderSecurityService.canAccessOrder(orderCode, currentUser)) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Bạn không có quyền thao tác trên đơn hàng này."
            ));
        }

        try {
            CancelOrderResponse resp = onlineOrderService.cancelOnlineOrder(orderCode, request, currentUser);
            return ResponseEntity.ok(resp);
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn(">>> [UC28] Không thể hủy đơn {}: {}", orderCode, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error(">>> [UC28] Lỗi hệ thống khi hủy đơn {}: {}", orderCode, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra trong quá trình hủy đơn. Vui lòng liên hệ hotline quán."
            ));
        }
    }

    /**
     * Look up orders by phone or order code (Secured against phone enumeration & IDOR).
     */
    @PostMapping("/lookup")
    public ResponseEntity<Map<String, Object>> lookupOrders(
            @RequestBody OrderLookupRequest request,
            Authentication authentication) {
        User currentUser = resolveCurrentUser(authentication);
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Vui lòng đăng nhập để tra cứu đơn hàng."
            ));
        }

        Map<String, Object> response = new HashMap<>();

        if (request.getOrderCode() != null && !request.getOrderCode().trim().isEmpty()) {
            String code = request.getOrderCode().trim().toUpperCase();
            if (!orderSecurityService.canAccessOrder(code, currentUser)) {
                log.warn(">>> [SECURITY IDOR BLOCK] User '{}' cố tra cứu đơn '{}' qua lookup API",
                        currentUser.getUsername(), code);
                response.put("success", false);
                response.put("message", "Bạn không có quyền truy cập thông tin đơn hàng này.");
                return ResponseEntity.status(403).body(response);
            }

            try {
                OrderTrackingDto order = orderTrackingService.getTrackingDetails(code);
                response.put("success", true);
                response.put("orders", List.of(order));
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", "Không tìm thấy đơn hàng với mã: " + code);
                return ResponseEntity.ok(response);
            }
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            String requestedPhone = request.getPhoneNumber().trim().replaceAll("[^0-9]", "");
            String userPhone = currentUser.getPhone() != null ? currentUser.getPhone().trim().replaceAll("[^0-9]", "") : "";

            boolean isStaffOrAdmin = currentUser.getRoles() != null && currentUser.getRoles().stream().anyMatch(r ->
                    "ROLE_ADMIN".equalsIgnoreCase(r) || "ADMIN".equalsIgnoreCase(r)
                            || "ROLE_MANAGER".equalsIgnoreCase(r) || "MANAGER".equalsIgnoreCase(r)
                            || "ROLE_STAFF".equalsIgnoreCase(r) || "STAFF".equalsIgnoreCase(r));

            if (!isStaffOrAdmin && (userPhone.isEmpty() || !userPhone.equals(requestedPhone))) {
                log.warn(">>> [SECURITY IDOR BLOCK] User '{}' cố tra cứu số điện thoại '{}' của người khác",
                        currentUser.getUsername(), request.getPhoneNumber());
                response.put("success", false);
                response.put("message", "Bạn chỉ có thể tra cứu đơn hàng bằng số điện thoại của chính mình.");
                return ResponseEntity.status(403).body(response);
            }

            List<OrderTrackingDto> orders = orderTrackingService.lookupOrdersByPhone(request.getPhoneNumber().trim());
            if (!isStaffOrAdmin) {
                orders = orders.stream()
                        .filter(o -> orderSecurityService.canAccessOrder(o.getOrderCode(), currentUser))
                        .toList();
            }

            response.put("success", !orders.isEmpty());
            response.put("orders", orders);
            if (orders.isEmpty()) {
                response.put("message", "Không tìm thấy đơn hàng nào với số điện thoại: " + request.getPhoneNumber());
            }
            return ResponseEntity.ok(response);
        }

        response.put("success", false);
        response.put("message", "Vui lòng nhập mã đơn hàng hoặc số điện thoại.");
        return ResponseEntity.badRequest().body(response);
    }

    private User resolveCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            return userRepository.findByUsername(authentication.getName()).orElse(null);
        }
        return null;
    }
}
