package com.ute.trasua.service;

import com.ute.trasua.dto.CartDto;
import com.ute.trasua.dto.CartItemDto;
import com.ute.trasua.dto.CartValidateRequest;

import java.util.List;

/**
 * Service interface for Customer Cart validation, calculation, and management (Module 1 - UC04).
 * Phân tách rõ ràng các trách nhiệm theo từng giai đoạn của giỏ hàng.
 */
public interface CartService {

    /**
     * 1. Xác thực và tính giá cho một ly trà sữa đơn lẻ (Product + Size + Toppings + Chi nhánh).
     * Phục vụ tính giá tức thời khi tùy chỉnh món trên Modal trước khi thêm vào giỏ.
     *
     * @param rawItem Món thô từ client
     * @param branchId ID chi nhánh đang chọn (nếu có)
     * @return CartItemDto đã được định giá chuẩn từ Database kèm itemKey
     */
    CartItemDto validateAndPriceItem(CartItemDto rawItem, Long branchId);

    /**
     * 2. Xác thực và tính toán giá trị của toàn bộ danh sách món trong giỏ hàng.
     * Sử dụng Batch Fetching để nạp dữ liệu một lần, tránh N+1 Database Query.
     *
     * @param items Danh sách các món trong giỏ
     * @param branchId ID chi nhánh đang chọn (nếu có)
     * @return CartDto chứa danh sách món hợp lệ, tổng số lượng và tạm tính (chưa trừ voucher)
     */
    CartDto validateCartItems(List<CartItemDto> items, Long branchId);

    /**
     * 3. Áp dụng mã Voucher khuyến mãi vào giỏ hàng đã được định giá.
     * Kiểm tra điều kiện chi nhánh, hạn mức đơn tối thiểu và tính số tiền giảm.
     *
     * @param cart Giỏ hàng đã được định giá tạm tính
     * @param voucherCode Mã khuyến mãi cần áp dụng
     * @param branchId ID chi nhánh đang đặt
     * @return CartDto cập nhật số tiền giảm và tổng thanh toán cuối cùng
     */
    CartDto applyVoucherToCart(CartDto cart, String voucherCode, Long branchId);

    /**
     * 4. Gỡ bỏ mã Voucher ra khỏi giỏ hàng và khôi phục nguyên giá đơn hàng.
     *
     * @param cart Giỏ hàng hiện tại
     * @return CartDto đã gỡ mã giảm giá
     */
    CartDto removeVoucherFromCart(CartDto cart);

    /**
     * 5. Phương thức điều phối toàn diện (Full Workflow Orchestration):
     * Tự động gọi: validateCartItems -> applyVoucherToCart -> hoàn tất CartDto.
     *
     * @param request Payload hợp lệ hóa giỏ hàng từ Client
     * @return CartDto hoàn chỉnh với đơn giá và cảnh báo
     */
    CartDto validateAndCalculateCart(CartValidateRequest request);
}
