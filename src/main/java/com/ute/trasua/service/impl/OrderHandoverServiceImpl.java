package com.ute.trasua.service.impl;

import com.ute.trasua.dto.handover.HandoverActionRequest;
import com.ute.trasua.dto.handover.HandoverOrderDto;
import com.ute.trasua.dto.handover.HandoverSlipDto;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.Driver;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.OrderDetail;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.repository.DriverRepository;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.service.OrderHandoverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Triển khai dịch vụ quản lý bàn giao và điều phối đơn hàng (Module 8 - UC09, UC18).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderHandoverServiceImpl implements OrderHandoverService {

    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final BranchRepository branchRepository;
    private final com.ute.trasua.service.OrderWebSocketPublisher orderWebSocketPublisher;
    private final com.ute.trasua.service.DriverDispatchService driverDispatchService;

    @Override
    @Transactional(readOnly = true)
    public List<HandoverOrderDto> getPendingHandovers(Long branchId) {
        Long targetBranchId = (branchId != null) ? branchId : 1L;

        List<String> readyStatuses = List.of("READY_FOR_DELIVERY", "READY_FOR_PICKUP", "PAID");
        List<Order> orders = orderRepository.findByBranchIdAndStatusInOrderByCreatedAtDesc(targetBranchId, readyStatuses);

        return orders.stream()
                .filter(o -> {
                    String st = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
                    return !"COMPLETED".equals(st) && !"CANCELLED".equals(st) && !"DELIVERED".equals(st);
                })
                .map(this::mapToHandoverOrderDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HandoverOrderDto handoverToDriver(Long orderId, Long driverId, String notes) {
        // Ủy quyền trực tiếp cho DriverDispatchService làm nguồn sự thật duy nhất (Single Source of Truth)
        driverDispatchService.dispatchMandatoryOrder(orderId, driverId, notes);
        Order saved = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));
        return mapToHandoverOrderDto(saved);
    }

    @Override
    @Transactional
    public HandoverOrderDto handoverToCustomer(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        String currentStatus = (order.getStatus() != null) ? order.getStatus().toUpperCase() : "";
        if ("CANCELLED".equals(currentStatus) || "COMPLETED".equals(currentStatus)) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái hợp lệ để bàn giao cho khách: " + currentStatus);
        }

        order.setStatus("COMPLETED");
        order.setDeliveredAt(LocalDateTime.now());
        if (order.getHandedOverAt() == null) {
            order.setHandedOverAt(LocalDateTime.now());
        }

        Order saved = orderRepository.save(order);
        log.info(">>> Đã bàn giao trực tiếp đơn hàng {} (STT #{}) cho khách hàng {}",
                saved.getOrderCode(), saved.getDailyQueueNumber(), saved.getCustomerName());

        try {
            orderWebSocketPublisher.broadcastOrderCompleted(saved);
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket hoàn tất đơn hàng: {}", e.getMessage());
        }

        return mapToHandoverOrderDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HandoverSlipDto getHandoverSlipData(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        Branch branch = order.getBranch();
        Driver driver = order.getDriver();
        User driverUser = (driver != null) ? driver.getUser() : null;

        List<String> itemsList = new ArrayList<>();
        int totalCups = 0;

        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                int qty = (d.getQuantity() != null && d.getQuantity() > 0) ? d.getQuantity() : 1;
                totalCups += qty;

                StringBuilder sb = new StringBuilder();
                sb.append(d.getProductName());
                if (d.getSizeName() != null && !d.getSizeName().isBlank()) {
                    sb.append(" (Size ").append(d.getSizeName()).append(")");
                }
                sb.append(" x").append(qty);

                if (d.getToppings() != null && !d.getToppings().isEmpty()) {
                    List<String> toppingNames = d.getToppings().stream()
                            .map(t -> t.getToppingName() != null ? t.getToppingName() : (t.getTopping() != null ? t.getTopping().getName() : ""))
                            .filter(name -> name != null && !name.isBlank())
                            .collect(Collectors.toList());
                    if (!toppingNames.isEmpty()) {
                        sb.append(" [").append(String.join(", ", toppingNames)).append("]");
                    }
                }
                itemsList.add(sb.toString());
            }
        }

        return HandoverSlipDto.builder()
                .slipCode("QLGH_BM1")
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .branchName(branch != null ? branch.getName() : "UTETra")
                .branchAddress(branch != null ? branch.getAddress() : "")
                .branchPhone(branch != null ? branch.getHotline() : "")
                .driverName(driverUser != null ? driverUser.getFullName() : (driver != null ? "Tài xế #" + driver.getId() : "Chưa chỉ định"))
                .driverPhone(driverUser != null ? driverUser.getPhone() : "")
                .vehiclePlate(driver != null ? driver.getLicensePlate() : "")
                .customerName(order.getCustomerName() != null ? order.getCustomerName() : "Khách hàng")
                .customerPhone(order.getCustomerPhone() != null ? order.getCustomerPhone() : "")
                .deliveryAddress(order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "")
                .itemsList(itemsList)
                .totalCups(totalCups > 0 ? totalCups : 1)
                .handedOverAt(order.getHandedOverAt() != null ? order.getHandedOverAt() : LocalDateTime.now())
                .handedOverBy(order.getCashierUsername() != null ? order.getCashierUsername() : "Điều phối viên")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HandoverOrderDto> getDriverOrders(Long driverId, List<String> statuses) {
        List<Order> orders;
        if (statuses == null || statuses.isEmpty()) {
            orders = orderRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
        } else {
            orders = orderRepository.findByDriverIdAndStatusInOrderByCreatedAtDesc(driverId, statuses);
        }

        return orders.stream()
                .map(this::mapToHandoverOrderDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HandoverOrderDto updateDeliveryStatus(Long orderId, Long driverId, String status, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        String upperStatus = (status != null) ? status.toUpperCase() : "";

        if ("COMPLETED".equals(upperStatus) || "DELIVERED".equals(upperStatus)) {
            order.setStatus("COMPLETED");
            order.setDeliveredAt(LocalDateTime.now());

            Driver driver = order.getDriver();
            if (driver == null && driverId != null) {
                driver = driverRepository.findById(driverId).orElse(null);
            }
            if (driver != null) {
                int currentCompleted = (driver.getCompletedOrders() != null) ? driver.getCompletedOrders() : 0;
                driver.setCompletedOrders(currentCompleted + 1);

                // Kiểm tra xem tài xế còn đơn nào khác đang giao trong hàng đợi không
                List<Order> remaining = orderRepository.findByDriverIdAndStatusInOrderByCreatedAtDesc(
                        driver.getId(), List.of("OUT_FOR_DELIVERY", "DELIVERING", "ARRIVED"));
                boolean hasOtherActive = remaining.stream().anyMatch(o -> !o.getId().equals(order.getId()));
                driver.setWorkingStatus(hasOtherActive ? "DELIVERING" : "ONLINE");
                driverRepository.save(driver);
            }
        } else if ("INCIDENT".equals(upperStatus) || "FAILED".equals(upperStatus) || "DELIVERY_FAILED".equals(upperStatus)) {
            order.setStatus("DELIVERY_FAILED");
            String safeNote = (note != null && !note.isBlank()) ? note.trim() : "Không có mô tả chi tiết";
            String existingNotes = order.getDeliveryNotes();
            if (existingNotes != null && !existingNotes.isBlank()) {
                order.setDeliveryNotes(existingNotes + " | Sự cố: " + safeNote);
            } else {
                order.setDeliveryNotes("Sự cố: " + safeNote);
            }

            Driver driver = order.getDriver();
            if (driver != null) {
                driver.setWorkingStatus("ONLINE");
                driverRepository.save(driver);
            }
        } else {
            order.setStatus(status);
        }

        Order saved = orderRepository.save(order);
        log.info(">>> Đã cập nhật trạng thái giao hàng đơn {} sang: {}", saved.getOrderCode(), saved.getStatus());

        try {
            if ("DELIVERED".equals(upperStatus) || "COMPLETED".equals(upperStatus)) {
                orderWebSocketPublisher.broadcastOrderCompleted(saved);
            } else if ("DELIVERY_FAILED".equals(saved.getStatus())) {
                orderWebSocketPublisher.broadcastOrderCancelled(saved, note);
            } else {
                orderWebSocketPublisher.broadcastOrderDelivering(saved, saved.getDriver());
            }
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket cập nhật trạng thái đơn: {}", e.getMessage());
        }

        return mapToHandoverOrderDto(saved);
    }

    private HandoverOrderDto mapToHandoverOrderDto(Order order) {
        if (order == null) {
            return null;
        }

        int totalCups = 0;
        List<String> itemNames = new ArrayList<>();

        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                int qty = (d.getQuantity() != null && d.getQuantity() > 0) ? d.getQuantity() : 1;
                totalCups += qty;
                String sizePart = (d.getSizeName() != null && !d.getSizeName().isBlank()) ? " (" + d.getSizeName() + ")" : "";
                itemNames.add(d.getProductName() + sizePart + " x" + qty);
            }
        }

        String itemsSummary = String.join(", ", itemNames);

        Driver driver = order.getDriver();
        User driverUser = (driver != null) ? driver.getUser() : null;

        return HandoverOrderDto.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .dailyQueueNumber(order.getDailyQueueNumber() != null ? order.getDailyQueueNumber() : 1)
                .orderType(order.getOrderType() != null ? order.getOrderType() : "POS_WALK_IN")
                .status(order.getStatus())
                .customerName(order.getCustomerName() != null ? order.getCustomerName() : "Khách hàng")
                .customerPhone(order.getCustomerPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .latitude(order.getLatitude())
                .longitude(order.getLongitude())
                .deliveryNotes(order.getDeliveryNotes())
                .totalAmount(order.getTotalAmount())
                .finalAmount(order.getFinalAmount())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .cupCount(totalCups > 0 ? totalCups : 1)
                .itemsSummary(itemsSummary)
                .driverId(driver != null ? driver.getId() : null)
                .driverName(driverUser != null ? driverUser.getFullName() : (driver != null ? "Tài xế #" + driver.getId() : null))
                .driverPhone(driverUser != null ? driverUser.getPhone() : null)
                .vehiclePlate(driver != null ? driver.getLicensePlate() : null)
                .createdAt(order.getCreatedAt())
                .handedOverAt(order.getHandedOverAt())
                .deliveredAt(order.getDeliveredAt())
                .build();
    }
}
