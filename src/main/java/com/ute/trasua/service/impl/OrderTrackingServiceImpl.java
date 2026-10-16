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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

        switch (rawStatus) {
            case "PENDING":
            case "AWAITING_PREPARATION":
            case "PAID":
                statusStep = 1;
                statusTitle = "Đã Tiếp Nhận Đơn Hàng";
                statusDesc = "Đơn hàng của bạn đã được ghi nhận. Số thứ tự lấy nước #" + (order.getDailyQueueNumber() != null ? order.getDailyQueueNumber() : "--");
                break;
            case "PROCESSING":
            case "PREPARING":
                statusStep = 2;
                statusTitle = "Quầy Barista Đang Pha Chế";
                statusDesc = "Nghệ nhân pha chế đang tùy biến đúng độ ngọt, đá và topping theo đơn của bạn.";
                break;
            case "PREPARED":
            case "READY_FOR_DELIVERY":
            case "READY_FOR_PICKUP":
                statusStep = 2;
                statusTitle = "Đã Pha Chế Xong — Chờ Bàn Giao";
                statusDesc = "Phẩm trà đã hoàn thành và đóng gói cẩn thận, đang chờ bàn giao cho tài xế giao vận.";
                break;
            case "OUT_FOR_DELIVERY":
            case "DISPATCHED":
            case "DELIVERING":
            case "ARRIVED":
                statusStep = 3;
                statusTitle = "Tài Xế Đang Trên Đường Giao";
                statusDesc = "Tài xế đang di chuyển tới địa chỉ nhận. Vui lòng để ý điện thoại khi shipper đến.";
                break;
            case "COMPLETED":
            case "DELIVERED":
                statusStep = 4;
                statusTitle = "Đã Giao Hàng Thành Công";
                statusDesc = "Đơn hàng đã hoàn tất. Cảm ơn quý khách đã thưởng thức trà tại UTETra Zen Gallery!";
                break;
            case "CANCELLED":
            case "DELIVERY_FAILED":
            case "FAILED":
            case "INCIDENT":
                statusStep = -1;
                statusTitle = "Đơn Hàng Đã Bị Hủy / Sự Cố";
                statusDesc = order.getNote() != null ? order.getNote() : "Đơn hàng chưa thể hoàn tất theo dự kiến.";
                break;
            default:
                statusStep = 1;
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
                        .build());
            }
        }

        String formattedTime = order.getCreatedAt() != null
                ? order.getCreatedAt().format(TIME_FORMATTER)
                : "";

        return OrderTrackingDto.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .dailyQueueNumber(order.getDailyQueueNumber())
                .orderType(order.getOrderType())
                .status(rawStatus)
                .statusStep(statusStep)
                .statusTitle(statusTitle)
                .statusDescription(statusDesc)
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
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
                .items(items)
                .build();
    }
}
