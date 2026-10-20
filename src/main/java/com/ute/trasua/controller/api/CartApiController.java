package com.ute.trasua.controller.api;

import com.ute.trasua.dto.CartDto;
import com.ute.trasua.dto.CartItemDto;
import com.ute.trasua.dto.CartValidateRequest;
import com.ute.trasua.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API Controller for shopping cart calculation and validation (Module 1 - UC04).
 * Cung cấp cả endpoint đơn lẻ lẫn endpoint điều phối toàn diện.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
public class CartApiController {

    private final CartService cartService;

    /**
     * Endpoint điều phối toàn diện: xác thực sản phẩm + voucher + tổng tiền.
     * Ngăn ngừa hoàn toàn tình trạng can thiệp giá từ phía client trước khi đặt hàng.
     */
    @PostMapping("/validate")
    public ResponseEntity<CartDto> validateCart(@RequestBody CartValidateRequest request) {
        CartDto validatedCart = cartService.validateAndCalculateCart(request);
        return ResponseEntity.ok(validatedCart);
    }

    /**
     * Endpoint định giá cho từng ly trà sữa (Modal tùy chỉnh topping/size).
     */
    @PostMapping("/price-item")
    public ResponseEntity<CartItemDto> priceSingleItem(@RequestBody CartItemDto item,
                                                       @RequestParam(value = "branchId", required = false) Long branchId) {
        CartItemDto pricedItem = cartService.validateAndPriceItem(item, branchId);
        return ResponseEntity.ok(pricedItem);
    }

    /**
     * Endpoint áp dụng mã giảm giá trực tiếp vào giỏ hàng.
     */
    @PostMapping("/apply-voucher")
    public ResponseEntity<CartDto> applyVoucher(@RequestBody CartDto cart,
                                                @RequestParam("voucherCode") String voucherCode,
                                                @RequestParam(value = "branchId", required = false) Long branchId) {
        CartDto updatedCart = cartService.applyVoucherToCart(cart, voucherCode, branchId);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Endpoint gỡ mã giảm giá ra khỏi giỏ hàng.
     */
    @PostMapping("/remove-voucher")
    public ResponseEntity<CartDto> removeVoucher(@RequestBody CartDto cart) {
        CartDto updatedCart = cartService.removeVoucherFromCart(cart);
        return ResponseEntity.ok(updatedCart);
    }
}
