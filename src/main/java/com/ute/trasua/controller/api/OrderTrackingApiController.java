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

    /**
     * Gets real-time tracking data for a specific order.
     */
    @GetMapping("/tracking/{orderCode}")
    public ResponseEntity<OrderTrackingDto> getTrackingData(@PathVariable("orderCode") String orderCode) {
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
        try {
            User currentUser = null;
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
                currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
            }
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
     * Look up orders by phone or order code.
     */
    @PostMapping("/lookup")
    public ResponseEntity<Map<String, Object>> lookupOrders(@RequestBody OrderLookupRequest request) {
        Map<String, Object> response = new HashMap<>();

        if (request.getOrderCode() != null && !request.getOrderCode().trim().isEmpty()) {
            try {
                OrderTrackingDto order = orderTrackingService.getTrackingDetails(request.getOrderCode().trim());
                response.put("success", true);
                response.put("orders", List.of(order));
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", "Không tìm thấy đơn hàng với mã: " + request.getOrderCode());
                return ResponseEntity.ok(response);
            }
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            List<OrderTrackingDto> orders = orderTrackingService.lookupOrdersByPhone(request.getPhoneNumber().trim());
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
}
