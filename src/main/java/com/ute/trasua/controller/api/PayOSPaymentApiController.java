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

    /**
     * Khởi tạo hoặc lấy lại link thanh toán PayOS còn hạn của đơn hàng.
     */
    @PostMapping("/payos/create-link")
    public ResponseEntity<PayOSCreatePaymentResponse> createOrGetPaymentLink(@RequestParam("orderCode") String orderCode) {
        log.info(">>> API yêu cầu tạo/lấy link thanh toán PayOS cho đơn: {}", orderCode);
        PayOSCreatePaymentResponse response = payosService.createOrGetPaymentLink(orderCode);
        return ResponseEntity.ok(response);
    }

    /**
     * API Polling kiểm tra trạng thái thanh toán (đặc biệt hữu dụng khi máy user bị lag / mất mạng).
     */
    @GetMapping("/status/{orderCode}")
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@PathVariable("orderCode") String orderCode) {
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
    public ResponseEntity<PayOSCreatePaymentResponse> skipPaymentForTesting(@RequestParam("orderCode") String orderCode) {
        log.info(">>> API kích hoạt Dev Test Skip Payment cho đơn: {}", orderCode);
        PayOSCreatePaymentResponse response = payosService.skipPaymentForTesting(orderCode);
        return ResponseEntity.ok(response);
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
