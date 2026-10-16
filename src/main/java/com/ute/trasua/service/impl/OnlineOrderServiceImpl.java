package com.ute.trasua.service.impl;

import com.ute.trasua.dto.CartDto;
import com.ute.trasua.dto.CartItemDto;
import com.ute.trasua.dto.CartItemToppingDto;
import com.ute.trasua.dto.VoucherApplyResponse;
import com.ute.trasua.dto.order.CheckoutRequest;
import com.ute.trasua.dto.order.CheckoutResponse;
import com.ute.trasua.entity.*;
import com.ute.trasua.repository.*;
import com.ute.trasua.service.CartService;
import com.ute.trasua.service.OnlineOrderService;
import com.ute.trasua.service.PromotionService;
import com.ute.trasua.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of OnlineOrderService handling authoritative order creation and persistence (Module 3 - UC05).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OnlineOrderServiceImpl implements OnlineOrderService {

    private final OrderRepository orderRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final SizeRepository sizeRepository;
    private final ToppingRepository toppingRepository;
    private final CartService cartService;
    private final ShippingService shippingService;
    private final PromotionService promotionService;
    private final com.ute.trasua.service.OrderWebSocketPublisher orderWebSocketPublisher;

    @Override
    @Transactional
    public CheckoutResponse createOnlineOrder(CheckoutRequest request, User currentUser) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Khay trà của bạn đang trống! Vui lòng chọn ít nhất 1 phẩm trà.");
        }

        // 1. Xác định chi nhánh phục vụ
        List<Branch> activeBranches = branchRepository.findByActiveTrue();
        if (activeBranches.isEmpty()) {
            throw new IllegalStateException("Hệ thống hiện không có chi nhánh nào đang mở cửa.");
        }

        Branch branch;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .filter(Branch::isActive)
                    .orElse(activeBranches.get(0));
        } else if (request.getLatitude() != null && request.getLongitude() != null) {
            branch = shippingService.findNearestBranch(request.getLatitude(), request.getLongitude(), activeBranches);
        } else {
            branch = activeBranches.get(0);
        }

        // 2. Định giá máy chủ độc quyền (Authoritative Server-side Price Verification)
        CartDto validatedCart = cartService.validateCartItems(request.getItems(), branch.getId());
        if (validatedCart.getItems() == null || validatedCart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Tất cả phẩm trà trong giỏ hàng hiện không khả dụng tại chi nhánh này.");
        }

        BigDecimal subtotal = validatedCart.getSubtotalAmount();

        // 3. Tính toán khoảng cách và cước vận chuyển chuẩn xác (Haversine Distance Engine)
        double distanceKm = 0.0;
        BigDecimal shippingFee = BigDecimal.ZERO;

        if (request.getLatitude() != null && request.getLongitude() != null &&
            branch.getLatitude() != null && branch.getLongitude() != null) {

            distanceKm = shippingService.calculateDistanceKm(
                    branch.getLatitude(), branch.getLongitude(),
                    request.getLatitude(), request.getLongitude()
            );
            shippingFee = shippingService.calculateShippingFee(distanceKm);
        }

        // 4. Kiểm tra và áp dụng mã giảm giá (nếu có)
        BigDecimal discount = BigDecimal.ZERO;
        String appliedVoucher = null;
        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            String cleanCode = request.getVoucherCode().trim().toUpperCase();
            try {
                VoucherApplyResponse voucherResp = promotionService.validateAndApplyVoucher(cleanCode, branch.getId(), subtotal);
                if (voucherResp.isValid()) {
                    discount = voucherResp.getDiscountAmount() != null ? voucherResp.getDiscountAmount() : BigDecimal.ZERO;
                    appliedVoucher = cleanCode;
                } else {
                    log.warn("Voucher {} không hợp lệ: {}", cleanCode, voucherResp.getMessage());
                }
            } catch (Exception e) {
                log.warn("Lỗi xác thực voucher {}: {}", cleanCode, e.getMessage());
            }
        }

        // 5. Tính tổng thanh toán cuối cùng: subtotal - discount + shippingFee
        BigDecimal finalAmount = subtotal.subtract(discount).add(shippingFee);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        // 6. Sinh số thứ tự lấy nước trong ngày và mã đơn hàng duy nhất
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        long countToday = orderRepository.countByBranchIdAndCreatedAtBetween(branch.getId(), startOfDay, endOfDay);
        int queueNumber = (int) countToday + 1;

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String orderCode = String.format("ORD-WEB-%s-%04d", dateStr, queueNumber);

        // 7. Khởi tạo đối tượng Order
        Order order = Order.builder()
                .orderCode(orderCode)
                .dailyQueueNumber(queueNumber)
                .orderType("ONLINE_DELIVERY")
                .status("AWAITING_PREPARATION")
                .paymentStatus("UNPAID")
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "PAYOS")
                .branch(branch)
                .user(currentUser)
                .customerName(request.getCustomerName().trim())
                .customerPhone(request.getCustomerPhone().trim())
                .deliveryAddress(request.getDeliveryAddress().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .deliveryNotes(request.getNote())
                .totalAmount(subtotal)
                .discountAmount(discount)
                .shippingFee(shippingFee)
                .distanceKm(BigDecimal.valueOf(distanceKm))
                .voucherCode(appliedVoucher)
                .finalAmount(finalAmount)
                .orderDetails(new ArrayList<>())
                .build();

        // 8. Đóng gói chi tiết từng ly trà sữa (OrderDetail & OrderDetailTopping)
        for (CartItemDto itemDto : validatedCart.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId()).orElse(null);
            Size size = sizeRepository.findByName(itemDto.getSizeCode()).orElse(null);

            String safeSizeName = itemDto.getSizeName();
            if (safeSizeName != null && safeSizeName.length() > 100) {
                safeSizeName = safeSizeName.substring(0, 100);
            }
            String safeSugar = itemDto.getSugarLevel();
            if (safeSugar != null && safeSugar.length() > 50) {
                safeSugar = safeSugar.substring(0, 50);
            }
            String safeIce = itemDto.getIceLevel();
            if (safeIce != null && safeIce.length() > 50) {
                safeIce = safeIce.substring(0, 50);
            }

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .productName(itemDto.getProductName())
                    .size(size)
                    .sizeName(safeSizeName)
                    .quantity(itemDto.getQuantity())
                    .sugarLevel(safeSugar != null ? safeSugar : "100%")
                    .iceLevel(safeIce != null ? safeIce : "100%")
                    .unitPrice(itemDto.getUnitPrice())
                    .subtotal(itemDto.getSubtotal())
                    .note(itemDto.getNote())
                    .build();

            if (itemDto.getToppings() != null) {
                for (CartItemToppingDto topDto : itemDto.getToppings()) {
                    Topping topping = toppingRepository.findById(topDto.getToppingId()).orElse(null);
                    OrderDetailTopping detailTopping = OrderDetailTopping.builder()
                            .orderDetail(detail)
                            .topping(topping)
                            .toppingName(topDto.getToppingName())
                            .price(topDto.getPrice())
                            .build();
                    detail.getToppings().add(detailTopping);
                }
            }

            order.getOrderDetails().add(detail);
        }

        // 9. Lưu đơn hàng xuống CSDL
        Order savedOrder = orderRepository.save(order);
        log.info(">>> Tạo đơn hàng trực tuyến thành công: {} (STT #{}) | Chi nhánh: {} | Khách: {} | Phí ship: {} | Tổng: {}",
                savedOrder.getOrderCode(), savedOrder.getDailyQueueNumber(), branch.getName(),
                savedOrder.getCustomerName(), savedOrder.getShippingFee(), savedOrder.getFinalAmount());

        // 10. Phát sự kiện WebSocket STOMP thời gian thực tới Khách Hàng và Bếp KDS
        try {
            orderWebSocketPublisher.broadcastOrderCreated(savedOrder);
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket sự kiện đơn mới: {}", e.getMessage());
        }

        return CheckoutResponse.builder()
                .success(true)
                .message("Đặt hàng trực tuyến thành công!")
                .orderId(savedOrder.getId())
                .orderCode(savedOrder.getOrderCode())
                .dailyQueueNumber(savedOrder.getDailyQueueNumber())
                .branchId(branch.getId())
                .branchName(branch.getName())
                .branchAddress(branch.getAddress())
                .customerName(savedOrder.getCustomerName())
                .customerPhone(savedOrder.getCustomerPhone())
                .deliveryAddress(savedOrder.getDeliveryAddress())
                .distanceKm(distanceKm)
                .shippingFee(savedOrder.getShippingFee())
                .subtotalAmount(savedOrder.getTotalAmount())
                .discountAmount(savedOrder.getDiscountAmount())
                .voucherCode(savedOrder.getVoucherCode())
                .finalAmount(savedOrder.getFinalAmount())
                .paymentMethod(savedOrder.getPaymentMethod())
                .paymentStatus(savedOrder.getPaymentStatus())
                .paymentUrl("/checkout/success?orderCode=" + savedOrder.getOrderCode())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByCode(String orderCode) {
        Order order = orderRepository.findWithDetailsByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + orderCode));

        // Nạp sẵn toàn bộ toppings của từng ly trà để JSP render không bị LazyInitializationException
        if (order.getOrderDetails() != null) {
            order.getOrderDetails().forEach(detail -> {
                if (detail.getToppings() != null) {
                    detail.getToppings().size();
                }
            });
        }
        return order;
    }
}
