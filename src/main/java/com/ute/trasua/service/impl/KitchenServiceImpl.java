package com.ute.trasua.service.impl;

import com.ute.trasua.dto.kitchen.KitchenOrderDto;
import com.ute.trasua.dto.kitchen.KitchenOrderItemDto;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.OrderDetail;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.service.KitchenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Triển khai dịch vụ tiếp nhận và pha chế đơn hàng KDS Barista (UC08).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenServiceImpl implements KitchenService {

    private final OrderRepository orderRepository;
    private final com.ute.trasua.service.BranchMenuService branchMenuService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM");

    @Override
    @Transactional
    public List<KitchenOrderDto> getKitchenOrders(Long branchId) {
        Long targetBranchId = (branchId != null) ? branchId : 1L;

        List<String> activeStatuses = List.of(
                "PAID", "AWAITING_PREPARATION",
                "PREPARING",
                "PREPARED", "READY_FOR_PICKUP", "READY_FOR_DELIVERY"
        );

        List<Order> orders = orderRepository.findByBranchIdAndStatusInOrderByCreatedAtAsc(targetBranchId, activeStatuses);
        
        // Auto-advance logic: Đơn hàng đầu tiên cần pha chế tự động chuyển sang PREPARING nếu đang ở trạng thái PAID / AWAITING_PREPARATION
        for (Order o : orders) {
            String st = (o.getStatus() != null) ? o.getStatus().toUpperCase() : "";
            if ("PAID".equals(st) || "AWAITING_PREPARATION".equals(st) || "PENDING".equals(st)) {
                o.setStatus("PREPARING");
                orderRepository.save(o);
                log.info(">>> [KDS AUTO-ADVANCE] Đơn hàng đầu tiên {} (STT #{}) tự động chuyển sang PREPARING", o.getOrderCode(), o.getDailyQueueNumber());
                break;
            } else if ("PREPARING".equals(st)) {
                // Đã có đơn đang làm việc ở top
                break;
            }
        }

        return orders.stream()
                .map(this::mapToKitchenOrderDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public KitchenOrderDto acceptOrderForPreparation(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        String currentStatus = (order.getStatus() != null) ? order.getStatus().toUpperCase() : "";
        if ("CANCELLED".equals(currentStatus) || "COMPLETED".equals(currentStatus) || "DELIVERED".equals(currentStatus)) {
            throw new IllegalStateException("Không thể tiếp nhận pha chế đơn hàng đã ở trạng thái: " + currentStatus);
        }

        order.setStatus("PREPARING");
        Order saved = orderRepository.save(order);

        log.info(">>> Barista đã tiếp nhận pha chế đơn {} (STT: #{})", saved.getOrderCode(), saved.getDailyQueueNumber());

        KitchenOrderDto dto = mapToKitchenOrderDto(saved);

        // Broadcast realtime
        broadcastKitchenUpdate(saved.getBranch() != null ? saved.getBranch().getId() : 1L, "ORDER_PREPARING", dto);

        return dto;
    }

    @Override
    @Transactional
    public KitchenOrderDto completePreparation(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        String currentStatus = (order.getStatus() != null) ? order.getStatus().toUpperCase() : "";
        if ("CANCELLED".equals(currentStatus) || "COMPLETED".equals(currentStatus) || "DELIVERED".equals(currentStatus)) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái hợp lệ để hoàn tất pha chế: " + currentStatus);
        }

        Long targetBranchId = (order.getBranch() != null) ? order.getBranch().getId() : 1L;

        // Tự động khấu trừ tồn kho Ly theo Size (M, L) và Topping của từng ly trong đơn
        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                int qty = (d.getQuantity() != null && d.getQuantity() > 0) ? d.getQuantity() : 1;
                String sizeName = (d.getSizeName() != null && !d.getSizeName().isBlank()) ? d.getSizeName().trim().toUpperCase() : "M";

                // Trừ tồn kho Ly cho từng cốc
                branchMenuService.deductCupStock(targetBranchId, sizeName, qty);

                // Trừ tồn kho Món nước (Product) trong kho thực đơn chi nhánh
                if (d.getProduct() != null) {
                    branchMenuService.deductProductStock(targetBranchId, d.getProduct().getId(), qty);
                }

                // Trừ tồn kho Topping trong kho thực đơn chi nhánh
                if (d.getToppings() != null) {
                    for (var top : d.getToppings()) {
                        String topName = top.getToppingName() != null ? top.getToppingName() : (top.getTopping() != null ? top.getTopping().getName() : "");
                        if (!topName.isBlank()) {
                            branchMenuService.deductToppingStock(targetBranchId, topName, qty);
                        }
                    }
                }
            }
        }

        // Phân loại luồng theo loại đơn:
        // Đơn giao hàng -> Chuyển sang READY_FOR_DELIVERY (Sẵn sàng điều phối / bàn giao cho Shipper)
        // Đơn tại quầy / Đặt trước đến lấy -> Chuyển sang READY_FOR_PICKUP (Bàn giao trực tiếp cho khách)
        String orderType = (order.getOrderType() != null) ? order.getOrderType().toUpperCase() : "POS_WALK_IN";
        if (orderType.contains("DELIVERY") || orderType.contains("SHIPPER") || orderType.contains("ONLINE")) {
            order.setStatus("READY_FOR_DELIVERY");
        } else {
            order.setStatus("READY_FOR_PICKUP");
        }

        Order saved = orderRepository.save(order);

        log.info(">>> Barista hoàn tất pha chế đơn {} (Trạng thái mới: {})", saved.getOrderCode(), saved.getStatus());

        KitchenOrderDto dto = mapToKitchenOrderDto(saved);

        // Broadcast realtime
        broadcastKitchenUpdate(targetBranchId, "ORDER_COMPLETED", dto);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public KitchenOrderDto getKitchenOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));
        return mapToKitchenOrderDto(order);
    }

    private KitchenOrderDto mapToKitchenOrderDto(Order order) {
        LocalDateTime createdAt = (order.getCreatedAt() != null) ? order.getCreatedAt() : LocalDateTime.now();
        long elapsedMinutes = Math.max(0, Duration.between(createdAt, LocalDateTime.now()).toMinutes());

        int totalCups = 0;
        List<KitchenOrderItemDto> items = new ArrayList<>();
        int cupIdx = 1;

        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                int qty = (d.getQuantity() != null && d.getQuantity() > 0) ? d.getQuantity() : 1;
                totalCups += qty;

                List<String> toppings = (d.getToppings() != null)
                        ? d.getToppings().stream().map(t -> t.getToppingName()).collect(Collectors.toList())
                        : new ArrayList<>();

                for (int q = 0; q < qty; q++) {
                    items.add(KitchenOrderItemDto.builder()
                            .orderDetailId(d.getId())
                            .cupIndex(cupIdx++)
                            .productId(d.getProduct() != null ? d.getProduct().getId() : null)
                            .productName(d.getProductName())
                            .sizeName(d.getSizeName() != null ? d.getSizeName() : "M")
                            .quantity(1)
                            .sugarLevel(d.getSugarLevel() != null ? d.getSugarLevel() : "100%")
                            .iceLevel(d.getIceLevel() != null ? d.getIceLevel() : "100%")
                            .toppings(toppings)
                            .note(d.getNote())
                            .build());
                }
            }
        }

        return KitchenOrderDto.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .dailyQueueNumber(order.getDailyQueueNumber() != null ? order.getDailyQueueNumber() : 1)
                .orderType(order.getOrderType() != null ? order.getOrderType() : "POS_WALK_IN")
                .status(order.getStatus())
                .cashierOrCustomerName(order.getCashierUsername() != null ? order.getCashierUsername() : "Khách tại quầy")
                .createdAtFormatted(createdAt.format(TIME_FMT))
                .elapsedMinutes(elapsedMinutes)
                .totalCups(totalCups > 0 ? totalCups : 1)
                .note(order.getNote())
                .items(items)
                .build();
    }

    private void broadcastKitchenUpdate(Long branchId, String eventType, KitchenOrderDto orderDto) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", eventType,
                    "branchId", branchId,
                    "order", orderDto,
                    "timestamp", System.currentTimeMillis()
            );
            messagingTemplate.convertAndSend("/topic/kitchen/" + branchId, (Object) payload);
        } catch (Exception e) {
            log.warn("Lỗi gửi WebSocket broadcast tới /topic/kitchen/{}: {}", branchId, e.getMessage());
        }
    }
}
