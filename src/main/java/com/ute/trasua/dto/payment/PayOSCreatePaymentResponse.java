package com.ute.trasua.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO kết quả khởi tạo hoặc lấy lại link thanh toán PayOS (UC06).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOSCreatePaymentResponse {

    private boolean success;
    private String message;

    private Long orderId;
    private String orderCode;
    private Long payosOrderCode;

    private BigDecimal amount;
    private String description;

    private String paymentLinkId;
    private String checkoutUrl;
    private String qrCode;

    private String accountNumber;
    private String accountName;
    private String bin;

    private LocalDateTime expiredAt;
    private Long remainingSeconds; // Số giây còn lại thực tế của phiên thanh toán
    private boolean reused; // True nếu tái sử dụng phiên thanh toán cũ còn hiệu lực
    private boolean simulated; // True nếu chạy chế độ giả lập test (chưa cấu hình PayOS)
}
