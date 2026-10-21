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
import java.time.LocalDate;
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
    private final com.ute.trasua.service.OrderWebSocketPublisher orderWebSocketPublisher;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM");

    @Override
    @Transactional
    public List<KitchenOrderDto> getKitchenOrders(Long branchId) {
        Long targetBranchId = (branchId != null) ? branchId : 1L;

        List<String> activeStatuses = List.of(
                "PENDING", "AWAITING_PREPARATION", "PAID",
                "PREPARING",
                "PREPARED", "READY_FOR_PICKUP", "READY_FOR_DELIVERY"
        );

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<Order> orders = orderRepository.findByBranchIdAndStatusInAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(
                targetBranchId, activeStatuses, startOfDay);
        
        // Auto-advance logic: Đơn hàng đầu tiên cần pha chế tự động chuyển sang PREPARING nếu đang ở trạng thái PAID / AWAITING_PREPARATION / PENDING
        for (Order o : orders) {
            String st = (o.getStatus() != null) ? o.getStatus().toUpperCase() : "";
            boolean paid = "PAID".equalsIgnoreCase(o.getPaymentStatus());
            if (paid && ("PAID".equals(st) || "AWAITING_PREPARATION".equals(st))) {
                o.setStatus("PREPARING");
                orderRepository.save(o);
                log.info(">>> [KDS AUTO-ADVANCE] Đơn hàng đầu tiên {} (STT #{}) tự động chuyển sang PREPARING", o.getOrderCode(), o.getDailyQueueNumber());
                try {
                    orderWebSocketPublisher.broadcastOrderPreparing(o);
                } catch (Exception e) {
                    log.warn("Lỗi phát WebSocket PREPARING cho đơn auto-advance: {}", e.getMessage());
                }
                break;
            } else if ("PREPARING".equals(st)) {
                // Đã có đơn đang làm việc ở top
                break;
            }
        }

        return orders.stream()
                .filter(o -> "PAID".equalsIgnoreCase(o.getPaymentStatus()))
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

        if (!"PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            throw new IllegalStateException("Cannot start preparation for an unpaid order!");
        }

        order.setStatus("PREPARING");
        Order saved = orderRepository.save(order);

        log.info(">>> Barista đã tiếp nhận pha chế đơn {} (STT: #{})", saved.getOrderCode(), saved.getDailyQueueNumber());

        KitchenOrderDto dto = mapToKitchenOrderDto(saved);

        // Broadcast realtime
        broadcastKitchenUpdate(saved.getBranch() != null ? saved.getBranch().getId() : 1L, "ORDER_PREPARING", dto);
        try {
            orderWebSocketPublisher.broadcastOrderPreparing(saved);
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket tới khách hàng: {}", e.getMessage());
        }

        return dto;
    }

    @Override
    @Transactional
    public KitchenOrderDto completePreparation(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        String currentStatus = (order.getStatus() != null) ? order.getStatus().toUpperCase() : "";
        if (!"PREPARING".equals(currentStatus)) {
            throw new IllegalStateException("Order " + order.getOrderCode() + " is not in PREPARING state (current: " + currentStatus + ")");
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

        // Broadcast realtime hoàn tất pha chế đơn hiện tại
        broadcastKitchenUpdate(targetBranchId, "ORDER_COMPLETED", dto);
        try {
            orderWebSocketPublisher.broadcastOrderPrepared(saved);
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket sự kiện pha chế xong: {}", e.getMessage());
        }

        // TỰ ĐỘNG CHUYỂN ĐƠN KẾ TIẾP TRONG QUEUE SANG PREPARING (chỉ đơn đã thanh toán):
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<Order> remainingWaiting = orderRepository.findByBranchIdAndStatusInAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(
                targetBranchId, List.of("PENDING", "AWAITING_PREPARATION", "PAID"), startOfDay);
        Order nextOrder = remainingWaiting.stream()
                .filter(o -> "PAID".equalsIgnoreCase(o.getPaymentStatus()))
                .findFirst().orElse(null);
        if (nextOrder != null) {
            nextOrder.setStatus("PREPARING");
            Order savedNext = orderRepository.save(nextOrder);
            log.info(">>> [KDS AUTO-NEXT] Đơn tiếp theo {} (STT #{}) tự động chuyển sang PREPARING",
                    savedNext.getOrderCode(), savedNext.getDailyQueueNumber());

            KitchenOrderDto nextDto = mapToKitchenOrderDto(savedNext);
            broadcastKitchenUpdate(targetBranchId, "ORDER_PREPARING", nextDto);
            try {
                orderWebSocketPublisher.broadcastOrderPreparing(savedNext);
            } catch (Exception e) {
                log.warn("Lỗi phát WebSocket PREPARING cho đơn kế tiếp: {}", e.getMessage());
            }
        }

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

        String customerName = order.getCustomerName();
        String cashierOrCustomer = (customerName != null && !customerName.isBlank())
                ? customerName
                : (order.getCashierUsername() != null ? order.getCashierUsername() : "Khách tại quầy");
        String combinedNote = (order.getNote() != null && !order.getNote().isBlank())
                ? order.getNote()
                : order.getDeliveryNotes();

        String rawSt = (order.getStatus() != null) ? order.getStatus().toUpperCase() : "PENDING";
        String statusLabel;
        switch (rawSt) {
            case "PREPARING": statusLabel = "Đang Pha Chế"; break;
            case "READY_FOR_DELIVERY": statusLabel = "Chờ Bàn Giao Shipper"; break;
            case "READY_FOR_PICKUP": statusLabel = "Sẵn Sàng Nhận Tại Quầy"; break;
            case "PAID": statusLabel = "Đã Thanh Toán"; break;
            case "AWAITING_PREPARATION": statusLabel = "Chờ Pha Chế"; break;
            case "COMPLETED": statusLabel = "Hoàn Tất"; break;
            case "CANCELLED": statusLabel = "Đã Hủy"; break;
            default: statusLabel = "Đã Tiếp Nhận"; break;
        }

        return KitchenOrderDto.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .dailyQueueNumber(order.getDailyQueueNumber() != null ? order.getDailyQueueNumber() : 1)
                .orderType(order.getOrderType() != null ? order.getOrderType() : "POS_WALK_IN")
                .status(order.getStatus())
                .statusLabel(statusLabel)
                .cashierOrCustomerName(cashierOrCustomer)
                .createdAtFormatted(createdAt.format(TIME_FMT))
                .elapsedMinutes(elapsedMinutes)
                .totalCups(totalCups > 0 ? totalCups : 1)
                .note(combinedNote)
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
