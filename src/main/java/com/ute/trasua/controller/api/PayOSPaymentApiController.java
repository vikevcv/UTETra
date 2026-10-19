package com.ute.trasua.controller.api;

import com.ute.trasua.dto.payment.PayOSCreatePaymentResponse;
import com.ute.trasua.dto.payment.PayOSWebhookPayload;
import com.ute.trasua.service.PayOSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API điều phối quy trình thanh toán PayOS VietQR (Module 2 - UC06):
 * - Tạo / Tái sử dụng Payment Link (hạn 15 phút, chống tạo rác)
 * - Tiếp nhận và xử lý Webhook IPN từ máy chủ PayOS (HMAC-SHA512)
 * - Kiểm tra polling trạng thái thanh toán cho client
 * - Kích hoạt nút Bỏ qua thanh toán (Dev Test)
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PayOSPaymentApiController {

    private final PayOSService payosService;
    private final com.ute.trasua.repository.UserRepository userRepository;
    private final com.ute.trasua.service.OrderSecurityService orderSecurityService;

    /**
     * Khởi tạo hoặc lấy lại link thanh toán PayOS còn hạn của đơn hàng.
     */
    @PostMapping("/payos/create-link")
    public ResponseEntity<?> createOrGetPaymentLink(
            @RequestParam("orderCode") String orderCode,
            org.springframework.security.core.Authentication authentication) {
        log.info(">>> API yêu cầu tạo/lấy link thanh toán PayOS cho đơn: {}", orderCode);
        com.ute.trasua.entity.User currentUser = resolveCurrentUser(authentication);
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Vui lòng đăng nhập để thực hiện thanh toán."
            ));
        }

        if (!orderSecurityService.canAccessOrder(orderCode, currentUser)) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Bạn không có quyền thanh toán đơn hàng này."
            ));
        }

        PayOSCreatePaymentResponse response = payosService.createOrGetPaymentLink(orderCode);
        return ResponseEntity.ok(response);
    }

    /**
     * API Polling kiểm tra trạng thái thanh toán (đặc biệt hữu dụng khi máy user bị lag / mất mạng).
     */
    @GetMapping("/status/{orderCode}")
    public ResponseEntity<?> checkPaymentStatus(
            @PathVariable("orderCode") String orderCode,
            org.springframework.security.core.Authentication authentication) {
        com.ute.trasua.entity.User currentUser = resolveCurrentUser(authentication);
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Vui lòng đăng nhập."
            ));
        }

        if (!orderSecurityService.canAccessOrder(orderCode, currentUser)) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Bạn không có quyền xem trạng thái đơn hàng này."
            ));
        }

        boolean isPaid = payosService.checkPaymentStatus(orderCode);
        Map<String, Object> result = new HashMap<>();
        result.put("orderCode", orderCode);
        result.put("paid", isPaid);
        result.put("paymentStatus", isPaid ? "PAID" : "UNPAID");
        return ResponseEntity.ok(result);
    }

    /**
     * Nút Bỏ Qua Thanh Toán (Dev Test) để kiểm thử toàn diện luồng sau thanh toán.
     */
    @PostMapping("/dev-skip")
    public ResponseEntity<?> skipPaymentForTesting(
            @RequestParam("orderCode") String orderCode,
            org.springframework.security.core.Authentication authentication) {
        log.info(">>> API kích hoạt Dev Test Skip Payment cho đơn: {}", orderCode);
        com.ute.trasua.entity.User currentUser = resolveCurrentUser(authentication);
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Vui lòng đăng nhập."
            ));
        }

        if (!orderSecurityService.canAccessOrder(orderCode, currentUser)) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Bạn không có quyền thao tác trên đơn hàng này."
            ));
        }

        PayOSCreatePaymentResponse response = payosService.skipPaymentForTesting(orderCode);
        return ResponseEntity.ok(response);
    }

    private com.ute.trasua.entity.User resolveCurrentUser(org.springframework.security.core.Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            return userRepository.findByUsername(authentication.getName()).orElse(null);
        }
        return null;
    }

    /**
     * Webhook IPN tiếp nhận thông báo thanh toán thành công trực tiếp từ máy chủ PayOS.
     * Xác thực chữ ký và cập nhật đơn hàng ngay lập tức.
     */
    @PostMapping("/payos/webhook")
    public ResponseEntity<Map<String, Object>> handlePayOSWebhook(@RequestBody(required = false) PayOSWebhookPayload payload) {
        log.info(">>> Nhận Webhook IPN từ PayOS: {}", payload);

        Map<String, Object> response = new HashMap<>();
        response.put("code", "00");
        response.put("desc", "success");

        if (payload == null || payload.getData() == null) {
            log.info(">>> [PayOS Webhook] Gói tin ping kiểm tra kết nối Webhook từ PayOS. Phản hồi 200 OK.");
            return ResponseEntity.ok(response);
        }

        try {
            boolean handled = payosService.handlePaymentWebhook(payload);
            log.info(">>> [PayOS Webhook] Xử lý giao dịch: {}", handled);
        } catch (Exception e) {
            log.warn(">>> [PayOS Webhook] Lỗi xử lý Webhook: {}", e.getMessage());
        }

        // Luôn trả về HTTP 200 OK để PayOS xác nhận Webhook URL hoạt động thành công
        return ResponseEntity.ok(response);
    }

    /**
     * Hỗ trợ GET /payos/webhook để kiểm tra kết nối từ trình duyệt hoặc công cụ test
     */
    @GetMapping("/payos/webhook")
    public ResponseEntity<Map<String, Object>> pingPayOSWebhook() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", "00");
        response.put("desc", "PayOS Webhook endpoint is active and listening");
        return ResponseEntity.ok(response);
    }
}
