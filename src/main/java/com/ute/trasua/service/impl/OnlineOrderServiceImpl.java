package com.ute.trasua.service.impl;

import com.ute.trasua.dto.CartDto;
import com.ute.trasua.dto.CartItemDto;
import com.ute.trasua.dto.CartItemToppingDto;
import com.ute.trasua.dto.VoucherApplyResponse;
import com.ute.trasua.dto.order.CancelOrderRequest;
import com.ute.trasua.dto.order.CancelOrderResponse;
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
    private final PaymentRepository paymentRepository;
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

        // Chặn đặt hàng nếu có bất kỳ món nào đang Tạm Ngưng tại chi nhánh được chọn
        for (CartItemDto item : validatedCart.getItems()) {
            if (Boolean.FALSE.equals(item.getAvailable())) {
                throw new IllegalArgumentException("Phẩm trà '" + item.getProductName() + "' hiện đang tạm ngưng phục vụ tại chi nhánh " + branch.getName() + ". Vui lòng xóa món này khỏi giỏ hàng trước khi đặt.");
            }
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
                VoucherApplyResponse voucherResp = promotionService.validateAndApplyVoucher(cleanCode, branch.getId(), subtotal, currentUser);
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

        boolean isZeroAmount = finalAmount.compareTo(BigDecimal.ZERO) == 0;
        String initialStatus = isZeroAmount ? "AWAITING_PREPARATION" : "AWAITING_PAYMENT";
        String initialPaymentStatus = isZeroAmount ? "PAID" : "UNPAID";
        String paymentMethod = (isZeroAmount && appliedVoucher != null && !appliedVoucher.isBlank())
                ? "VOUCHER"
                : (request.getPaymentMethod() != null ? request.getPaymentMethod() : "PAYOS");

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
                .status(initialStatus)
                .paymentStatus(initialPaymentStatus)
                .paidAt(isZeroAmount ? LocalDateTime.now() : null)
                .paymentMethod(paymentMethod)
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

        // Tiêu thụ lượt dùng voucher (Atomic) nếu đơn hàng áp dụng voucher
        if (appliedVoucher != null && !appliedVoucher.isBlank()) {
            boolean consumed = promotionService.consumeVoucherUsage(appliedVoucher);
            if (!consumed) {
                log.warn(">>> Cảnh báo: Voucher {} không thể tiêu thụ (đã hết lượt hoặc không tồn tại)", appliedVoucher);
            }
        }

        // Tự động tạo bản ghi Payment nếu đơn 0đ (thanh toán hoàn tất bằng voucher)
        if (isZeroAmount) {
            Payment payment = Payment.builder()
                    .order(savedOrder)
                    .paymentMethod(savedOrder.getPaymentMethod())
                    .amount(BigDecimal.ZERO)
                    .transactionCode("VOUCHER-" + savedOrder.getOrderCode())
                    .status("SUCCESS")
                    .paidAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);
        }

        // 10. Phát sự kiện WebSocket STOMP thời gian thực tới Khách Hàng và Bếp KDS
        try {
            orderWebSocketPublisher.broadcastOrderCreated(savedOrder);
            if (isZeroAmount) {
                orderWebSocketPublisher.broadcastOnlineOrderPaid(savedOrder);
            }
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket sự kiện đơn mới: {}", e.getMessage());
        }

        String paymentUrl = isZeroAmount
                ? "/checkout/success?orderCode=" + savedOrder.getOrderCode()
                : "/checkout/payment?orderCode=" + savedOrder.getOrderCode();

        return CheckoutResponse.builder()
                .success(true)
                .message(isZeroAmount ? "Đặt hàng thành công! Đơn hàng 0đ đã được tự động thanh toán qua voucher." : "Đặt hàng trực tuyến thành công!")
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
                .paymentUrl(paymentUrl)
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

    @Override
    @Transactional
    public CancelOrderResponse cancelOnlineOrder(String orderCode, CancelOrderRequest request, User currentUser) {
        if (orderCode == null || orderCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn hàng không hợp lệ.");
        }

        String cleanCode = orderCode.trim();
        Order order = orderRepository.findByOrderCode(cleanCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + cleanCode));

        // 1. Kiểm tra trạng thái: Nghiêm ngặt chỉ cho phép hủy khi chưa bắt đầu pha chế (PENDING, AWAITING_PAYMENT, AWAITING_PREPARATION, PAID)
        String currentStatus = order.getStatus() != null ? order.getStatus().trim().toUpperCase() : "PENDING";
        boolean isCancelable = "PENDING".equals(currentStatus)
                || "AWAITING_PAYMENT".equals(currentStatus)
                || "AWAITING_PREPARATION".equals(currentStatus)
                || "PAID".equals(currentStatus);
        if (!isCancelable) {
            if ("CANCELLED".equals(currentStatus)) {
                throw new IllegalStateException("Đơn hàng này đã được hủy trước đó.");
            }
            throw new IllegalStateException("Đơn hàng đã được quầy đưa vào pha chế hoặc đang vận chuyển, không thể hủy.");
        }

        // 2. Kiểm tra quyền sở hữu (nếu người dùng đăng nhập)
        if (currentUser != null && order.getUser() != null) {
            boolean isOwner = order.getUser().getId().equals(currentUser.getId());
            boolean isStaff = currentUser.getRoles() != null && currentUser.getRoles().stream()
                    .anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r) || "ROLE_MANAGER".equalsIgnoreCase(r));
            if (!isOwner && !isStaff) {
                throw new IllegalStateException("Bạn không có quyền hủy đơn hàng này.");
            }
        }

        // 3. Ghi nhận lý do hủy đơn
        String reason = (request != null && request.getReason() != null && !request.getReason().trim().isEmpty())
                ? request.getReason().trim()
                : "Khách hàng yêu cầu hủy đơn trực tuyến";

        String cancelLog = "[Khách hủy đơn: " + reason + "]";
        if (order.getDeliveryNotes() != null && !order.getDeliveryNotes().isEmpty()) {
            order.setDeliveryNotes(order.getDeliveryNotes() + " | " + cancelLog);
        } else {
            order.setDeliveryNotes(cancelLog);
        }
        order.setStatus("CANCELLED");

        // 4. Phục hồi lượt sử dụng voucher cũ (nếu đơn có dùng voucher)
        boolean restoredOldVoucher = false;
        String restoredVoucherCode = null;
        if (order.getVoucherCode() != null && !order.getVoucherCode().trim().isEmpty()) {
            try {
                promotionService.restoreVoucherUsage(order.getVoucherCode().trim());
                restoredOldVoucher = true;
                restoredVoucherCode = order.getVoucherCode().trim().toUpperCase();
                log.info(">>> [CANCEL ORDER] Đã phục hồi lượt sử dụng voucher cũ: {} cho đơn {}", restoredVoucherCode, order.getOrderCode());
            } catch (Exception e) {
                log.warn(">>> [CANCEL ORDER] Không thể phục hồi voucher cũ {}: {}", order.getVoucherCode(), e.getMessage());
            }
        }

        // 5. Kiểm tra trạng thái thanh toán để quyết định cấp voucher bồi hoàn hay không:
        // Chỉ cấp voucher bồi hoàn khi đơn ĐÃ THANH TOÁN và có phát sinh số tiền thực tế (> 0)
        boolean isPaid = "PAID".equalsIgnoreCase(order.getPaymentStatus())
                && order.getFinalAmount() != null
                && order.getFinalAmount().compareTo(BigDecimal.ZERO) > 0;
        Promotion compVoucher = null;
        String expiryFormatted = null;
        String message;

        if (isPaid) {
            // Đơn ĐÃ THANH TOÁN (UC28): Tự động sinh voucher bồi hoàn 80% (Triple-Shield Anti-Collision, ngẫu nhiên hoàn toàn 10 ký tự, hạn 30 ngày)
            compVoucher = promotionService.createCompensationVoucher(order, new BigDecimal("0.80"), 30);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            expiryFormatted = compVoucher.getEndDate() != null
                    ? compVoucher.getEndDate().format(dtf)
                    : "30 ngày sau";
            message = "Hủy đơn hàng thành công! Quý khách được bồi hoàn voucher 80% giá trị đơn hàng.";
            log.info(">>> [UC28] Hủy đơn hàng ĐÃ THANH TOÁN {} thành công. Đã cấp voucher bồi hoàn {} ({}đ)",
                    order.getOrderCode(), compVoucher.getVoucherCode(), compVoucher.getDiscountValue());
        } else {
            // Đơn CHƯA THANH TOÁN: Tuyệt đối KHÔNG sinh voucher bồi hoàn. Hoàn trả voucher cũ nếu có.
            message = restoredOldVoucher
                    ? "Hủy đơn hàng chưa thanh toán thành công! Mã ưu đãi [" + restoredVoucherCode + "] đã được hoàn trả lại cho quý khách."
                    : "Hủy đơn hàng chưa thanh toán thành công!";
            log.info(">>> [CANCEL UNPAID] Hủy đơn hàng CHƯA THANH TOÁN {}. Không cấp voucher bồi hoàn. Phục hồi voucher cũ: {}",
                    order.getOrderCode(), restoredOldVoucher ? restoredVoucherCode : "Không có");
        }

        // 6. Lưu trạng thái đơn hàng đã hủy vào DB ngay lập tức
        Order savedOrder = orderRepository.saveAndFlush(order);

        // 7. Đồng bộ WebSocket STOMP tới Khách Hàng, Bếp KDS và Bàn giao
        try {
            orderWebSocketPublisher.broadcastOrderCancelled(savedOrder, reason);
        } catch (Exception e) {
            log.warn(">>> [UC28] Lỗi phát WebSocket hủy đơn: {}", e.getMessage());
        }


        return CancelOrderResponse.builder()
                .success(true)
                .orderCode(savedOrder.getOrderCode())
                .status("CANCELLED")
                .compensationVoucherCode(compVoucher != null ? compVoucher.getVoucherCode() : null)
                .compensationAmount(compVoucher != null ? compVoucher.getDiscountValue() : BigDecimal.ZERO)
                .expiryDateFormatted(expiryFormatted)
                .restoredOldVoucher(restoredOldVoucher)
                .restoredVoucherCode(restoredVoucherCode)
                .message(message)
                .build();
    }
}
