package com.ute.trasua.service;

import com.ute.trasua.dto.payment.PayOSCreatePaymentResponse;
import com.ute.trasua.dto.payment.PayOSWebhookPayload;

/**
 * Service interface xử lý cổng thanh toán trực tuyến PayOS (UC06).
 */
public interface PayOSService {

    /**
     * Khởi tạo hoặc tái sử dụng link thanh toán VietQR PayOS cho đơn hàng (hạn 15 phút).
     * Áp dụng cơ chế Idempotent: Nếu phiên thanh toán cũ vẫn còn hiệu lực > 30s, trả về thông tin cũ
     * để tránh tạo rác trên cổng PayOS và đảm bảo mã QR cố định cho khách hàng.
     *
     * @param orderCode Mã đơn hàng (VD: ORD-WEB-261019-0001)
     * @return PayOSCreatePaymentResponse chứa link thanh toán, mã VietQR và trạng thái
     */
    PayOSCreatePaymentResponse createOrGetPaymentLink(String orderCode);

    /**
     * Xác thực chữ ký số HMAC-SHA512 từ Webhook của PayOS.
     *
     * @param payload Webhook payload JSON nhận từ PayOS
     * @return true nếu chữ ký hợp lệ và toàn vẹn dữ liệu
     */
    boolean verifyWebhookSignature(PayOSWebhookPayload payload);

    /**
     * Xử lý Webhook IPN từ PayOS khi thanh toán thành công:
     * - Cập nhật trạng thái đơn hàng sang PAID và AWAITING_PREPARATION
     * - Ghi nhận lịch sử giao dịch vào bảng Payment
     * - Kích hoạt WebSocket STOMP báo quầy POS, Bếp KDS và Khách hàng
     *
     * @param payload Webhook payload JSON nhận từ PayOS
     * @return true nếu xử lý thành công
     */
    boolean handlePaymentWebhook(PayOSWebhookPayload payload);

    /**
     * Nút Bỏ Qua Thanh Toán (Dev Test) phục vụ kiểm thử nhanh các tính năng sau thanh toán:
     * - Tự động cập nhật đơn hàng thành PAID
     * - Lưu Payment loại PAYOS_DEV_SKIP
     * - Bắn WebSocket STOMP báo quầy POS và Bếp y như thanh toán thật
     *
     * @param orderCode Mã đơn hàng cần test
     * @return PayOSCreatePaymentResponse với trạng thái thành công
     */
    PayOSCreatePaymentResponse skipPaymentForTesting(String orderCode);

    /**
     * Kiểm tra trạng thái thanh toán hiện tại của đơn hàng (dùng cho Polling phía client).
     *
     * @param orderCode Mã đơn hàng
     * @return true nếu đơn đã được thanh toán (PAID)
     */
    boolean checkPaymentStatus(String orderCode);

    /**
     * Chủ động đối soát trực tiếp trạng thái thanh toán từ PayOS Cloud (Active Reconciliation).
     * Hữu dụng đặc biệt trong môi trường dev localhost (không nhận được Webhook) hoặc khi mạng chập chờn.
     * Tự động cập nhật Database sang PAID, lưu Payment và phát WebSocket STOMP nếu PayOS xác nhận đã nhận tiền.
     *
     * @param orderCode Mã đơn hàng
     * @return true nếu đơn hàng đã được thanh toán thành công
     */
    boolean syncPaymentStatusFromPayOS(String orderCode);
}
