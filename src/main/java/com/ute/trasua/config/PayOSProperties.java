package com.ute.trasua.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình tham số kết nối Cổng thanh toán trực tuyến PayOS (UC06).
 */
@Configuration
@ConfigurationProperties(prefix = "payos")
@Data
public class PayOSProperties {

    /**
     * Client ID từ PayOS Dashboard (Bỏ trống ban đầu nếu chưa tạo).
     */
    private String clientId;

    /**
     * API Key bí mật từ PayOS Dashboard.
     */
    private String apiKey;

    /**
     * Checksum Key dùng cho việc tạo và xác thực chữ ký số HMAC-SHA512.
     */
    private String checksumKey;

    /**
     * URL chuyển hướng sau khi khách thanh toán thành công trên PayOS.
     */
    private String returnUrl = "http://localhost:8080/checkout/payos/return";

    /**
     * URL chuyển hướng khi khách hủy thanh toán trên PayOS.
     */
    private String cancelUrl = "http://localhost:8080/checkout/payos/cancel";

    /**
     * Kiểm tra xem hệ thống đã được cấu hình đủ các khóa PayOS thật hay chưa.
     */
    public boolean isConfigured() {
        return clientId != null && !clientId.trim().isEmpty()
                && apiKey != null && !apiKey.trim().isEmpty()
                && checksumKey != null && !checksumKey.trim().isEmpty();
    }
}
