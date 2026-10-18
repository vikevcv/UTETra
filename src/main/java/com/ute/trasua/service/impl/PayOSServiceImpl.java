package com.ute.trasua.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ute.trasua.config.PayOSProperties;
import com.ute.trasua.dto.payment.PayOSCreatePaymentResponse;
import com.ute.trasua.dto.payment.PayOSWebhookData;
import com.ute.trasua.dto.payment.PayOSWebhookPayload;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.Payment;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.repository.PaymentRepository;
import com.ute.trasua.service.OrderWebSocketPublisher;
import com.ute.trasua.service.PayOSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Triển khai dịch vụ thanh toán trực tuyến PayOS (Module 2 - UC06).
 * Quản lý vòng đời phiên thanh toán, mã hóa chữ ký số HMAC-SHA512,
 * xử lý Webhook IPN và đồng bộ trạng thái đơn hàng thời gian thực.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSServiceImpl implements PayOSService {

    private final PayOSProperties payosProperties;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderWebSocketPublisher orderWebSocketPublisher;
    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String PAYOS_API_ENDPOINT = "https://api-merchant.payos.vn/v2/payment-requests";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    @Transactional
    public PayOSCreatePaymentResponse createOrGetPaymentLink(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + orderCode));

        // Nếu đơn đã thanh toán xong
        if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            return PayOSCreatePaymentResponse.builder()
                    .success(true)
                    .message("Đơn hàng đã được thanh toán thành công.")
                    .orderId(order.getId())
                    .orderCode(order.getOrderCode())
                    .amount(order.getFinalAmount())
                    .reused(true)
                    .simulated(false)
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. Cơ chế Tái Sử Dụng Phiên Thanh Toán (Idempotent Payment Link):
        // Nếu đã có link và thời gian hết hạn còn trên 30 giây -> Tái sử dụng để tránh tạo rác và đổi mã QR
        if (order.getPayosCheckoutUrl() != null && order.getPaymentExpiredAt() != null
                && order.getPaymentExpiredAt().isAfter(now.plusSeconds(30))) {
            log.info(">>> [PayOS] Tái sử dụng link thanh toán cũ cho đơn {} (Hạn đến: {})",
                    order.getOrderCode(), order.getPaymentExpiredAt());

            long remainingSecs = Math.max(0, java.time.Duration.between(now, order.getPaymentExpiredAt()).getSeconds());

            return PayOSCreatePaymentResponse.builder()
                    .success(true)
                    .message("Tái sử dụng phiên thanh toán đang hiệu lực.")
                    .orderId(order.getId())
                    .orderCode(order.getOrderCode())
                    .payosOrderCode(order.getPayosOrderCode())
                    .amount(order.getFinalAmount())
                    .paymentLinkId(order.getPayosPaymentLinkId())
                    .checkoutUrl(order.getPayosCheckoutUrl())
                    .qrCode(order.getPayosQrCode())
                    .accountNumber(order.getPayosAccountNumber())
                    .accountName(order.getPayosAccountName())
                    .bin(order.getPayosBin())
                    .expiredAt(order.getPaymentExpiredAt())
                    .remainingSeconds(remainingSecs)
                    .reused(true)
                    .simulated(false)
                    .build();
        }

        // 2. Sinh mã số nguyên PayOS duy nhất nếu chưa có
        if (order.getPayosOrderCode() == null) {
            long numericCode = generateNumericOrderCode(order);
            order.setPayosOrderCode(numericCode);
        }

        // Thời hạn hiệu lực: 15 phút kể từ hiện tại
        LocalDateTime expiredAt = now.plusMinutes(15);
        long expiredAtEpoch = expiredAt.toEpochSecond(ZoneOffset.ofHours(7)); // Múi giờ Việt Nam UTC+7

        // 3. Kiểm tra xem người dùng đã cấu hình Client ID / API Key / Checksum Key chưa
        if (!payosProperties.isConfigured()) {
            log.warn(">>> [PayOS] Chưa cấu hình thông tin PayOS (ClientId/ApiKey/ChecksumKey). Chuyển sang chế độ demo giả lập.");

            String simulatedCheckoutUrl = payosProperties.getReturnUrl() + "?orderCode=" + order.getOrderCode() + "&status=PAID";
            String simulatedQr = "vietqr://pay?bank=MB&acc=0396202885&amount=" + order.getFinalAmount().intValue() + "&des=" + order.getOrderCode();

            order.setPayosPaymentLinkId("DEV-SIM-" + order.getPayosOrderCode());
            order.setPayosCheckoutUrl(simulatedCheckoutUrl);
            order.setPayosQrCode(simulatedQr);
            order.setPaymentExpiredAt(expiredAt);
            orderRepository.save(order);

            return PayOSCreatePaymentResponse.builder()
                    .success(true)
                    .message("Hệ thống đang hoạt động ở chế độ thử nghiệm (Chưa cấu hình API Key PayOS thật). Bạn có thể bấm nút 'Bỏ qua thanh toán (Dev Test)' để trải nghiệm luồng sau thanh toán.")
                    .orderId(order.getId())
                    .orderCode(order.getOrderCode())
                    .payosOrderCode(order.getPayosOrderCode())
                    .amount(order.getFinalAmount())
                    .paymentLinkId(order.getPayosPaymentLinkId())
                    .checkoutUrl(simulatedCheckoutUrl)
                    .qrCode(simulatedQr)
                    .accountNumber("0396202885")
                    .accountName("UTETRA TEA GALLERY")
                    .bin("970422")
                    .expiredAt(expiredAt)
                    .remainingSeconds(Math.max(0, java.time.Duration.between(now, expiredAt).getSeconds()))
                    .reused(false)
                    .simulated(true)
                    .build();
        }

        // 4. Gọi API máy chủ PayOS để tạo Payment Link thật
        try {
            int amount = order.getFinalAmount().intValue();
            String description = "DH " + order.getPayosOrderCode();
            if (description.length() > 25) {
                description = description.substring(0, 25);
            }

            // Ký số HMAC-SHA256 theo chuẩn bảo mật PayOS: amount={}&cancelUrl={}&description={}&orderCode={}&returnUrl={}
            String signatureData = String.format("amount=%d&cancelUrl=%s&description=%s&orderCode=%d&returnUrl=%s",
                    amount,
                    payosProperties.getCancelUrl(),
                    description,
                    order.getPayosOrderCode(),
                    payosProperties.getReturnUrl()
            );

            String signature = hmacSha256(payosProperties.getChecksumKey().trim(), signatureData);

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("orderCode", order.getPayosOrderCode());
            requestBody.put("amount", amount);
            requestBody.put("description", description);
            requestBody.put("buyerName", order.getCustomerName() != null ? order.getCustomerName() : "Khách hàng");
            requestBody.put("buyerPhone", order.getCustomerPhone() != null ? order.getCustomerPhone() : "");
            requestBody.put("buyerAddress", order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "");
            requestBody.put("returnUrl", payosProperties.getReturnUrl());
            requestBody.put("cancelUrl", payosProperties.getCancelUrl());
            requestBody.put("expiredAt", expiredAtEpoch);
            requestBody.put("signature", signature);

            String jsonPayload = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PAYOS_API_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("x-client-id", payosProperties.getClientId().trim())
                    .header("x-api-key", payosProperties.getApiKey().trim())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String code = root.path("code").asText();
                if ("00".equals(code)) {
                    JsonNode data = root.path("data");
                    String paymentLinkId = data.path("paymentLinkId").asText();
                    String checkoutUrl = data.path("checkoutUrl").asText();
                    String qrCode = data.path("qrCode").asText();
                    String accountNumber = data.path("accountNumber").asText();
                    String accountName = data.path("accountName").asText();
                    String bin = data.path("bin").asText();

                    order.setPayosPaymentLinkId(paymentLinkId);
                    order.setPayosCheckoutUrl(checkoutUrl);
                    order.setPayosQrCode(qrCode);
                    order.setPayosAccountNumber(accountNumber);
                    order.setPayosAccountName(accountName);
                    order.setPayosBin(bin);
                    order.setPaymentExpiredAt(expiredAt);
                    orderRepository.save(order);

                    log.info(">>> [PayOS] Tạo Payment Link thành công cho đơn {}: linkId={}",
                            order.getOrderCode(), paymentLinkId);

                    return PayOSCreatePaymentResponse.builder()
                            .success(true)
                            .message("Tạo link thanh toán PayOS thành công!")
                            .orderId(order.getId())
                            .orderCode(order.getOrderCode())
                            .payosOrderCode(order.getPayosOrderCode())
                            .amount(order.getFinalAmount())
                            .paymentLinkId(paymentLinkId)
                            .checkoutUrl(checkoutUrl)
                            .qrCode(qrCode)
                            .accountNumber(accountNumber)
                            .accountName(accountName)
                            .bin(bin)
                            .expiredAt(expiredAt)
                            .remainingSeconds(Math.max(0, java.time.Duration.between(now, expiredAt).getSeconds()))
                            .reused(false)
                            .simulated(false)
                            .build();
                } else {
                    String desc = root.path("desc").asText();
                    throw new RuntimeException("PayOS từ chối yêu cầu: " + desc);
                }
            } else {
                throw new RuntimeException("Lỗi kết nối PayOS (Mã HTTP " + response.statusCode() + "): " + response.body());
            }

        } catch (Exception e) {
            log.error(">>> [PayOS] Lỗi khi tạo link thanh toán: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo phiên thanh toán PayOS: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyWebhookSignature(PayOSWebhookPayload payload) {
        if (payload == null || payload.getData() == null || payload.getSignature() == null) {
            return false;
        }

        if (!payosProperties.isConfigured()) {
            log.warn("PayOS chưa cấu hình checksumKey, bỏ qua kiểm tra chữ ký ở môi trường test.");
            return true;
        }

        try {
            // Chuyển Data sang Map để sort alphabet các keys
            Map<String, Object> dataMap = objectMapper.convertValue(payload.getData(), Map.class);
            SortedMap<String, Object> sortedMap = new TreeMap<>(dataMap);

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> entry : sortedMap.entrySet()) {
                if (entry.getValue() != null) {
                    if (sb.length() > 0) sb.append("&");
                    sb.append(entry.getKey()).append("=").append(entry.getValue());
                }
            }

            String sig256 = hmacSha256(payosProperties.getChecksumKey().trim(), sb.toString());
            String sig512 = hmacSha512(payosProperties.getChecksumKey().trim(), sb.toString());
            boolean valid = sig256.equalsIgnoreCase(payload.getSignature().trim())
                    || sig512.equalsIgnoreCase(payload.getSignature().trim());

            if (!valid) {
                log.warn(">>> [PayOS Webhook] Chữ ký không khớp! Nhận: {}, Tính SHA256: {}, Tính SHA512: {}",
                        payload.getSignature(), sig256, sig512);
            }
            return valid;
        } catch (Exception e) {
            log.error("Lỗi xác thực chữ ký Webhook PayOS: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public boolean handlePaymentWebhook(PayOSWebhookPayload payload) {
        if (!verifyWebhookSignature(payload)) {
            log.warn(">>> [PayOS Webhook] Thẩm định chữ ký thất bại. Từ chối IPN.");
            return false;
        }

        PayOSWebhookData data = payload.getData();
        if (data == null || data.getOrderCode() == null) {
            log.warn(">>> [PayOS Webhook] Thiếu thông tin orderCode trong dữ liệu IPN.");
            return true;
        }

        Long payosCode = data.getOrderCode();
        Order order = orderRepository.findByPayosOrderCode(payosCode)
                .orElse(null);

        if (order == null) {
            // Đây là gói tin Test Probe / Confirm Webhook từ PayOS Portal (orderCode mẫu như 123)
            log.info(">>> [PayOS Webhook] Nhận gói tin Test Probe / Xác thực Webhook từ PayOS (orderCode: {}). Phản hồi thành công.", payosCode);
            return true;
        }

        // Kiểm tra xem đơn đã được xác nhận PAID trước đó chưa (Idempotent Webhook)
        if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            log.info(">>> [PayOS Webhook] Đơn hàng {} đã ở trạng thái PAID từ trước. Bỏ qua ghi nhận trùng.", order.getOrderCode());
            return true;
        }

        // Cập nhật trạng thái đơn hàng sang PAID và AWAITING_PREPARATION
        order.setPaymentStatus("PAID");
        order.setStatus("AWAITING_PREPARATION");
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);

        // Lưu bản ghi Payment
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod("PAYOS")
                .amount(data.getAmount() != null ? data.getAmount() : order.getFinalAmount())
                .transactionCode(data.getReference() != null ? data.getReference() : data.getPaymentLinkId())
                .status("SUCCESS")
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        log.info(">>> [PayOS Webhook] Đã xác thực thanh toán thành công cho đơn {}. Số tiền: {}",
                order.getOrderCode(), payment.getAmount());

        // Kích hoạt WebSocket STOMP báo quầy POS, Bếp KDS và Khách Hàng
        try {
            orderWebSocketPublisher.broadcastOnlineOrderPaid(order);
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket sau Webhook PayOS: {}", e.getMessage());
        }

        return true;
    }

    @Override
    @Transactional
    public PayOSCreatePaymentResponse skipPaymentForTesting(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng: " + orderCode));

        if (!"PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            order.setPaymentStatus("PAID");
            order.setStatus("AWAITING_PREPARATION");
            order.setPaidAt(LocalDateTime.now());
            orderRepository.save(order);

            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod("PAYOS_DEV_SKIP")
                    .amount(order.getFinalAmount())
                    .transactionCode("DEV-SKIP-" + System.currentTimeMillis())
                    .status("SUCCESS")
                    .paidAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);

            log.info(">>> [Dev Test] Đã kích hoạt giả lập thanh toán thành công cho đơn {}", order.getOrderCode());

            // Kích hoạt WebSocket STOMP báo quầy POS, Bếp KDS và Khách Hàng
            try {
                orderWebSocketPublisher.broadcastOnlineOrderPaid(order);
            } catch (Exception e) {
                log.warn("Lỗi phát WebSocket trong Dev Skip: {}", e.getMessage());
            }
        }

        return PayOSCreatePaymentResponse.builder()
                .success(true)
                .message("Đã giả lập thanh toán thành công cho môi trường kiểm thử!")
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .amount(order.getFinalAmount())
                .reused(true)
                .simulated(true)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkPaymentStatus(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .map(o -> "PAID".equalsIgnoreCase(o.getPaymentStatus()))
                .orElse(false);
    }

    /**
     * Sinh mã số nguyên PayOS duy nhất không đụng hàng (đảm bảo <= 9007199254740991).
     * Dạng: [yyMMdd][queueNumber 4 chữ số] (Ví dụ: 2610190001).
     */
    private long generateNumericOrderCode(Order order) {
        try {
            String oc = order.getOrderCode(); // VD: ORD-WEB-261019-0001
            String cleaned = oc.replaceAll("[^0-9]", "");
            if (cleaned.length() >= 6) {
                return Long.parseLong(cleaned);
            }
        } catch (Exception ignored) {
        }
        // Dự phòng: Timestamp modulo + order id
        return (System.currentTimeMillis() % 1000000000L) * 10 + (order.getId() != null ? (order.getId() % 10) : 1);
    }

    /**
     * Tiện ích mã hóa HMAC-SHA256 chuẩn PayOS (RFC 2104).
     */
    public static String hmacSha256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * bytes.length);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi sinh chữ ký HMAC-SHA256: " + e.getMessage(), e);
        }
    }

    /**
     * Tiện ích mã hóa HMAC-SHA512 chuẩn RFC 2104.
     */
    public static String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKey);
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * bytes.length);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi sinh chữ ký HMAC-SHA512: " + e.getMessage(), e);
        }
    }
}
