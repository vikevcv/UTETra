package com.ute.trasua.service;

import com.ute.trasua.dto.order.CancelOrderRequest;
import com.ute.trasua.dto.order.CancelOrderResponse;
import com.ute.trasua.dto.order.CheckoutRequest;
import com.ute.trasua.dto.order.CheckoutResponse;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.User;

/**
 * Service interface for online delivery checkout and order processing (Module 3 - UC05, UC28).
 */
public interface OnlineOrderService {

    /**
     * Creates an authoritative online delivery order with server-calculated prices,
     * Haversine shipping fee, voucher discounts, and order items.
     *
     * @param request Validated checkout payload from client
     * @param currentUser Logged-in customer (nullable for guest checkout)
     * @return CheckoutResponse with created order code and summary
     */
    CheckoutResponse createOnlineOrder(CheckoutRequest request, User currentUser);

    /**
     * Finds an order by its unique order code.
     *
     * @param orderCode Order code string (e.g. UTE-20261016-ABCD)
     * @return Order entity
     */
    Order getOrderByCode(String orderCode);

    /**
     * Hủy đơn hàng trực tuyến khi chưa bước vào pha chế (UC28).
     * Tự động sinh voucher bồi hoàn 80% hạn 30 ngày và hoàn trả voucher cũ (nếu có).
     *
     * @param orderCode Mã đơn hàng cần hủy
     * @param request Thông tin yêu cầu hủy (lý do, sđt)
     * @param currentUser Người dùng đang đăng nhập (hoặc null nếu khách vãng lai)
     * @return CancelOrderResponse chứa mã voucher bồi hoàn mới
     */
    CancelOrderResponse cancelOnlineOrder(String orderCode, CancelOrderRequest request, User currentUser);
}
