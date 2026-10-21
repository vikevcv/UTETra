package com.ute.trasua.service.impl;

import com.ute.trasua.dto.order.OrderItemTrackingDto;
import com.ute.trasua.dto.order.OrderTrackingDto;
import com.ute.trasua.entity.*;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.service.OrderTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of OrderTrackingService for customer live order status inspection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderTrackingServiceImpl implements OrderTrackingService {

    private final OrderRepository orderRepository;
    private final com.ute.trasua.repository.UserRepository userRepository;
    private final com.ute.trasua.repository.ProductReviewRepository productReviewRepository;
    private final com.ute.trasua.repository.OrderComplaintRepository orderComplaintRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy");

    @Override
    @Transactional(readOnly = true)
    public OrderTrackingDto getTrackingDetails(String orderCode) {
        if (orderCode == null || orderCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn hàng không được để trống");
        }

        String cleanCode = orderCode.trim().toUpperCase();
        Order order = orderRepository.findTrackingByOrderCode(cleanCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + cleanCode));

        return mapToTrackingDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderTrackingDto> lookupOrdersByPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String cleanPhone = phoneNumber.trim();
        List<Order> orders = orderRepository.findTop10ByCustomerPhoneOrderByCreatedAtDesc(cleanPhone);
        return orders.stream().map(this::mapToTrackingDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderTrackingDto> getRecentOrdersForUsername(String username) {
        if (username == null || username.trim().isEmpty() || "anonymousUser".equalsIgnoreCase(username)) {
            return new ArrayList<>();
        }

        User user = userRepository.findByUsername(username.trim()).orElse(null);
        if (user == null) {
            return new ArrayList<>();
        }

        List<Order> orders = new ArrayList<>(orderRepository.findTop20ByUserIdOrderByCreatedAtDesc(user.getId()));

        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            List<Order> phoneOrders = orderRepository.findTop10ByCustomerPhoneOrderByCreatedAtDesc(user.getPhone().trim());
            java.util.Set<String> existingCodes = orders.stream()
                    .map(Order::getOrderCode)
                    .collect(Collectors.toSet());
            for (Order po : phoneOrders) {
                if (po.getOrderCode() != null && !existingCodes.contains(po.getOrderCode())) {
                    orders.add(po);
                    existingCodes.add(po.getOrderCode());
                }
            }
        }

        orders.sort((a, b) -> {
            if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        return orders.stream().map(this::mapToTrackingDto).collect(Collectors.toList());
    }

    private OrderTrackingDto mapToTrackingDto(Order order) {
        // Map status step
        String rawStatus = order.getStatus() != null ? order.getStatus().toUpperCase() : "PENDING";
        int statusStep = 1;
        String statusTitle = "Đã Tiếp Nhận Đơn Hàng";
        String statusDesc = "Đơn hàng của bạn đã được ghi nhận vào hệ thống và đang chờ quầy xử lý.";

        String statusLabel = "Đã Tiếp Nhận";
        switch (rawStatus) {
            case "AWAITING_PAYMENT":
                statusStep = 1;
                statusLabel = "Chờ Thanh Toán";
                statusTitle = "Đang Chờ Quý Khách Thanh Toán";
                statusDesc = "Vui lòng quét mã VietQR PayOS để hoàn tất thanh toán. Quán sẽ tự động tiếp nhận pha chế ngay khi chuyển khoản thành công.";
                break;
            case "PENDING":
                statusStep = 1;
                statusLabel = "Đã Tiếp Nhận";
                statusTitle = "Đã Tiếp Nhận Đơn Hàng";
                statusDesc = "Đơn hàng của bạn đã được ghi nhận vào hệ thống và đang chờ quầy xử lý.";
                break;
            case "AWAITING_PREPARATION":
                statusStep = 1;
                statusLabel = "Chờ Pha Chế";
                statusTitle = "Đang Chờ Quầy Pha Chế";
                statusDesc = "Đơn hàng đã được xác nhận và xếp vào hàng đợi quầy pha chế.";
                break;
            case "PAID":
                statusStep = 1;
                statusLabel = "Đã Thanh Toán";
                statusTitle = "Đã Thanh Toán Thành Công";
                statusDesc = "Đơn hàng của bạn đã được ghi nhận. Số thứ tự lấy nước #" + (order.getDailyQueueNumber() != null ? order.getDailyQueueNumber() : "--");
                break;
            case "PROCESSING":
            case "PREPARING":
                statusStep = 2;
                statusLabel = "Đang Pha Chế";
                statusTitle = "Quầy Barista Đang Pha Chế";
                statusDesc = "Nghệ nhân pha chế đang tùy biến đúng độ ngọt, đá và topping theo đơn của bạn.";
                break;
            case "PREPARED":
                statusStep = 2;
                statusLabel = "Đã Pha Xong";
                statusTitle = "Đã Pha Chế Xong — Chờ Bàn Giao";
                statusDesc = "Phẩm trà đã hoàn thành và đóng gói cẩn thận, đang chờ bàn giao.";
                break;
            case "READY_FOR_DELIVERY":
                statusStep = 2;
                statusLabel = "Chờ Bàn Giao Shipper";
                statusTitle = "Đã Pha Xong — Chờ Bàn Giao Cho Tài Xế";
                statusDesc = "Phẩm trà đã đóng gói nguyên vẹn tem niêm phong, đang chờ tài xế tiếp nhận giao hàng.";
                break;
            case "READY_FOR_PICKUP":
                statusStep = 2;
                statusLabel = "Sẵn Sàng Nhận Tại Quầy";
                statusTitle = "Trà Đã Sẵn Sàng — Mời Quý Khách Nhận";
                statusDesc = "Phẩm trà đã hoàn thành tại quầy. Mời bạn đọc số thứ tự #" + (order.getDailyQueueNumber() != null ? order.getDailyQueueNumber() : "--") + " để nhận nước.";
                break;
            case "OUT_FOR_DELIVERY":
            case "DISPATCHED":
                statusStep = 3;
                statusLabel = "Đang Giao Hàng";
                statusTitle = "Tài Xế Đang Trên Đường Giao";
                statusDesc = "Tài xế đang di chuyển tới địa chỉ nhận. Vui lòng để ý điện thoại khi shipper đến.";
                break;
            case "DELIVERING":
                statusStep = 3;
                statusLabel = "Đang Giao Hàng";
                statusTitle = "Tài Xế Đang Di Chuyển Tới Bạn";
                statusDesc = "Tài xế đã xuất phát và đang trên đường giao hàng đến địa chỉ của bạn.";
                break;
            case "ARRIVED":
                statusStep = 3;
                statusLabel = "Tài Xế Đã Đến";
                statusTitle = "Tài Xế Đã Đến Điểm Giao";
                statusDesc = "Tài xế đã có mặt tại điểm giao hàng. Quý khách vui lòng để ý điện thoại và nhận trà!";
                break;
            case "COMPLETED":
            case "DELIVERED":
                statusStep = 4;
                statusLabel = "Giao Thành Công";
                statusTitle = "Đã Giao Hàng Thành Công";
                statusDesc = "Đơn hàng đã hoàn tất. Cảm ơn quý khách đã thưởng thức trà tại UTETra Zen Gallery!";
                break;
            case "CANCELLED":
            case "DELIVERY_FAILED":
            case "FAILED":
            case "INCIDENT":
                statusStep = -1;
                statusLabel = "Đã Hủy";
                statusTitle = "Đơn Hàng Đã Bị Hủy";
                if (order.getDeliveryNotes() != null && !order.getDeliveryNotes().isBlank()) {
                    statusDesc = order.getDeliveryNotes();
                } else if (order.getNote() != null && !order.getNote().isBlank()) {
                    statusDesc = order.getNote();
                } else {
                    statusDesc = "Rất tiếc vì sự cố này. Đơn hàng đã được ghi nhận hủy trên hệ thống.";
                }
                break;
            default:
                statusStep = 1;
                statusLabel = "Đã Tiếp Nhận";
                break;
        }

        // Branch info
        Branch branch = order.getBranch();
        Long branchId = branch != null ? branch.getId() : null;
        String branchName = branch != null ? branch.getName() : "Chi nhánh UTETra";
        String branchAddr = branch != null ? branch.getAddress() : "";
        String branchHotline = branch != null ? branch.getHotline() : "1900-UTETRA";
        Double branchLat = branch != null ? branch.getLatitude() : 10.85072;
        Double branchLng = branch != null ? branch.getLongitude() : 106.77196;

        // Driver info
        Driver driver = order.getDriver();
        Long driverId = null;
        String driverName = null;
        String driverPhone = null;
        String vehiclePlate = null;
        Double driverLat = null;
        Double driverLng = null;

        if (driver != null) {
            driverId = driver.getId();
            driverName = driver.getUser() != null ? driver.getUser().getFullName() : "Tài xế UTETra";
            driverPhone = driver.getUser() != null ? driver.getUser().getPhone() : "";
            vehiclePlate = driver.getLicensePlate();
        }

        // Items breakdown
        List<OrderItemTrackingDto> items = new ArrayList<>();
        Map<Long, com.ute.trasua.entity.ProductReview> reviewMap = new HashMap<>();
        try {
            if (productReviewRepository != null && order.getId() != null) {
                List<com.ute.trasua.entity.ProductReview> revList = productReviewRepository.findByOrderId(order.getId());
                for (com.ute.trasua.entity.ProductReview r : revList) {
                    if (r.getOrderDetail() != null) {
                        reviewMap.put(r.getOrderDetail().getId(), r);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Lỗi tải đánh giá đơn {}: {}", order.getOrderCode(), e.getMessage());
        }

        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                List<String> topNames = new ArrayList<>();
                if (d.getToppings() != null) {
                    for (OrderDetailTopping t : d.getToppings()) {
                        topNames.add(t.getToppingName());
                    }
                }

                String imgUrl = (d.getProduct() != null && d.getProduct().getImageUrl() != null)
                        ? d.getProduct().getImageUrl() : null;

                com.ute.trasua.entity.ProductReview rev = reviewMap.get(d.getId());
                boolean isRev = (rev != null);

                items.add(OrderItemTrackingDto.builder()
                        .id(d.getId())
                        .productId(d.getProduct() != null ? d.getProduct().getId() : null)
                        .productName(d.getProductName())
                        .imageUrl(imgUrl)
                        .sizeName(d.getSizeName())
                        .sugarLevel(d.getSugarLevel())
                        .iceLevel(d.getIceLevel())
                        .toppings(topNames)
                        .quantity(d.getQuantity())
                        .unitPrice(d.getUnitPrice())
                        .subtotal(d.getSubtotal())
                        .note(d.getNote())
                        .isReviewed(isRev)
                        .userRating(rev != null ? rev.getRating() : null)
                        .userComment(rev != null ? rev.getComment() : null)
                        .build());
            }
        }

        boolean allReviewed = !items.isEmpty() && items.stream().allMatch(it -> Boolean.TRUE.equals(it.getIsReviewed()));

        // Complaint breakdown
        boolean hasComplaint = false;
        String complaintStatus = null;
        String complaintType = null;
        String complaintTypeLabel = null;
        String complaintDescription = null;
        String complaintVoucherCode = null;
        BigDecimal complaintCompensationAmount = null;
        String complaintAdminResponse = null;

        try {
            if (orderComplaintRepository != null && order.getId() != null) {
                Optional<com.ute.trasua.entity.OrderComplaint> cOpt = orderComplaintRepository.findByOrderId(order.getId());
                if (cOpt.isPresent()) {
                    com.ute.trasua.entity.OrderComplaint c = cOpt.get();
                    hasComplaint = true;
                    complaintStatus = c.getStatus();
                    complaintType = c.getComplaintType();
                    complaintTypeLabel = mapComplaintTypeLabel(c.getComplaintType());
                    complaintDescription = c.getDescription();
                    complaintVoucherCode = c.getCompensationVoucherCode();
                    complaintCompensationAmount = c.getCompensationAmount();
                    complaintAdminResponse = c.getAdminResponse();
                }
            }
        } catch (Exception e) {
            log.warn("Lỗi tải khiếu nại đơn {}: {}", order.getOrderCode(), e.getMessage());
        }

        String formattedTime = order.getCreatedAt() != null
                ? order.getCreatedAt().format(TIME_FORMATTER)
                : "";

        boolean canCancel = "PENDING".equalsIgnoreCase(rawStatus)
                || "AWAITING_PAYMENT".equalsIgnoreCase(rawStatus)
                || "AWAITING_PREPARATION".equalsIgnoreCase(rawStatus)
                || "PAID".equalsIgnoreCase(rawStatus);

        boolean isPaid = "PAID".equalsIgnoreCase(order.getPaymentStatus());
        BigDecimal baseRefundAmount = order.getFinalAmount() != null ? order.getFinalAmount() : BigDecimal.ZERO;

        BigDecimal estimatedCompensationAmount = (isPaid && baseRefundAmount.compareTo(BigDecimal.ZERO) > 0)
                ? baseRefundAmount.multiply(new BigDecimal("0.80")).setScale(0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        boolean pendingPayment = !"PAID".equalsIgnoreCase(order.getPaymentStatus())
                && !"CANCELLED".equalsIgnoreCase(rawStatus)
                && !"DELIVERY_FAILED".equalsIgnoreCase(rawStatus)
                && !"FAILED".equalsIgnoreCase(rawStatus);

        return OrderTrackingDto.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .dailyQueueNumber(order.getDailyQueueNumber())
                .orderType(order.getOrderType())
                .status(rawStatus)
                .statusLabel(statusLabel)
                .statusStep(statusStep)
                .statusTitle(statusTitle)
                .statusDescription(statusDesc)
                .canCancel(canCancel)
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .pendingPayment(pendingPayment)
                .createdAt(formattedTime)
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryNotes(order.getDeliveryNotes())
                .customerLatitude(order.getLatitude())
                .customerLongitude(order.getLongitude())
                .branchId(branchId)
                .branchName(branchName)
                .branchAddress(branchAddr)
                .branchHotline(branchHotline)
                .branchLatitude(branchLat)
                .branchLongitude(branchLng)
                .driverId(driverId)
                .driverName(driverName)
                .driverPhone(driverPhone)
                .vehiclePlate(vehiclePlate)
                .driverLatitude(driverLat)
                .driverLongitude(driverLng)
                .distanceKm(order.getDistanceKm())
                .subtotalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .voucherCode(order.getVoucherCode())
                .finalAmount(order.getFinalAmount())
                .estimatedCompensationAmount(estimatedCompensationAmount)
                .items(items)
                .allReviewed(allReviewed)
                .hasComplaint(hasComplaint)
                .complaintStatus(complaintStatus)
                .complaintType(complaintType)
                .complaintTypeLabel(complaintTypeLabel)
                .complaintDescription(complaintDescription)
                .complaintVoucherCode(complaintVoucherCode)
                .complaintCompensationAmount(complaintCompensationAmount)
                .complaintAdminResponse(complaintAdminResponse)
                .build();
    }

    private String mapComplaintTypeLabel(String type) {
        if (type == null) return "Sự cố khác";
        switch (type.toUpperCase()) {
            case "SAI_MON": return "Giao sai món / Sai đường đá";
            case "THIEU_MON": return "Thiếu món / Thiếu topping";
            case "HU_HAI": return "Ly trà bị đổ vỡ / Hư hỏng";
            case "CHAT_LUONG": return "Chất lượng không đạt";
            case "THAI_DO": return "Thái độ phục vụ";
            default: return "Lý do khác";
        }
    }
}
