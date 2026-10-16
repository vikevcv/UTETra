package com.ute.trasua.controller.api;

import com.ute.trasua.dto.order.OrderLookupRequest;
import com.ute.trasua.dto.order.OrderTrackingDto;
import com.ute.trasua.service.OrderTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for order tracking data and phone-based lookups.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderTrackingApiController {

    private final OrderTrackingService orderTrackingService;

    /**
     * Gets real-time tracking data for a specific order.
     */
    @GetMapping("/tracking/{orderCode}")
    public ResponseEntity<OrderTrackingDto> getTrackingData(@PathVariable("orderCode") String orderCode) {
        OrderTrackingDto dto = orderTrackingService.getTrackingDetails(orderCode);
        return ResponseEntity.ok(dto);
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
