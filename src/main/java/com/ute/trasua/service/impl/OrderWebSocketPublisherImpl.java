package com.ute.trasua.service.impl;

import com.ute.trasua.dto.websocket.OrderStatusEventDto;
import com.ute.trasua.entity.Driver;
import com.ute.trasua.entity.Order;
import com.ute.trasua.service.OrderWebSocketPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of OrderWebSocketPublisher using Spring SimpMessagingTemplate (STOMP broker).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderWebSocketPublisherImpl implements OrderWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    @Override
    public void broadcastOrderCreated(Order order) {
        if (order == null) return;

        OrderStatusEventDto event = OrderStatusEventDto.builder()
                .eventType("ORDER_CREATED")
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .branchId(order.getBranch() != null ? order.getBranch().getId() : 1L)
                .branchName(order.getBranch() != null ? order.getBranch().getName() : "Chi nhánh UTETra")
                .dailyQueueNumber(order.getDailyQueueNumber())
                .status("PENDING")
                .statusStep(1)
                .statusTitle("Đã Đặt Đơn Hàng Thành Công")
                .statusMessage("Đơn hàng đã được tiếp nhận và chuyển đến quầy pha chế của chi nhánh.")
                .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                .build();

        // 1. Gửi tới kênh riêng của khách hàng
        sendToCustomer(order.getOrderCode(), event);

        // 2. Gửi tín hiệu thông báo có đơn mới tới KDS quầy pha chế Barista
        Long branchId = order.getBranch() != null ? order.getBranch().getId() : 1L;
        sendToKitchen(branchId, event);
    }

    @Override
    public void broadcastOrderPreparing(Order order) {
        if (order == null) return;

        OrderStatusEventDto event = OrderStatusEventDto.builder()
                .eventType("PREPARING")
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .branchId(order.getBranch() != null ? order.getBranch().getId() : 1L)
                .branchName(order.getBranch() != null ? order.getBranch().getName() : "Chi nhánh UTETra")
                .dailyQueueNumber(order.getDailyQueueNumber())
                .status("PREPARING")
                .statusStep(2)
                .statusTitle("Quầy Barista Đang Pha Chế")
                .statusMessage("Nghệ nhân pha chế đang thực hiện tùy biến ly trà theo đúng yêu cầu của bạn.")
                .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                .build();

        sendToCustomer(order.getOrderCode(), event);
    }

    @Override
    public void broadcastOrderPrepared(Order order) {
        if (order == null) return;

        OrderStatusEventDto event = OrderStatusEventDto.builder()
                .eventType("PREPARED")
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .branchId(order.getBranch() != null ? order.getBranch().getId() : 1L)
                .branchName(order.getBranch() != null ? order.getBranch().getName() : "Chi nhánh UTETra")
                .dailyQueueNumber(order.getDailyQueueNumber())
                .status("PREPARED")
                .statusStep(2)
                .statusTitle("Pha Chế Hoàn Tất — Chờ Bàn Giao")
                .statusMessage("Ly trà đã được đóng gói kỹ lưỡng và chuyển sang bàn giao cho tài xế giao vận.")
                .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                .build();

        sendToCustomer(order.getOrderCode(), event);

        // Báo cho màn hình bàn giao chi nhánh
        Long branchId = order.getBranch() != null ? order.getBranch().getId() : 1L;
        sendToHandover(branchId, event);
    }

    @Override
    public void broadcastOrderDispatched(Order order, Driver driver) {
        if (order == null) return;

        String driverName = driver != null && driver.getUser() != null ? driver.getUser().getFullName() : "Tài xế UTETra";
        String driverPhone = driver != null && driver.getUser() != null ? driver.getUser().getPhone() : "";
        String vehiclePlate = driver != null ? driver.getLicensePlate() : "";

        OrderStatusEventDto event = OrderStatusEventDto.builder()
                .eventType("DISPATCHED")
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .branchId(order.getBranch() != null ? order.getBranch().getId() : 1L)
                .branchName(order.getBranch() != null ? order.getBranch().getName() : "Chi nhánh UTETra")
                .dailyQueueNumber(order.getDailyQueueNumber())
                .status("DELIVERING")
                .statusStep(3)
                .statusTitle("Tài Xế Đang Trên Đường Giao Hàng")
                .statusMessage("Tài xế " + driverName + " đã nhận phẩm trà và đang di chuyển tới địa chỉ nhận.")
                .driverId(driver != null ? driver.getId() : null)
                .driverName(driverName)
                .driverPhone(driverPhone)
                .vehiclePlate(vehiclePlate)
                .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                .build();

        sendToCustomer(order.getOrderCode(), event);

        if (driver != null) {
            sendToShipper(driver.getId(), event);
        }
    }

    @Override
    public void broadcastOrderDelivering(Order order, Driver driver) {
        broadcastOrderDispatched(order, driver);
    }

    @Override
    public void broadcastOrderCompleted(Order order) {
        if (order == null) return;

        OrderStatusEventDto event = OrderStatusEventDto.builder()
                .eventType("COMPLETED")
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .branchId(order.getBranch() != null ? order.getBranch().getId() : 1L)
                .branchName(order.getBranch() != null ? order.getBranch().getName() : "Chi nhánh UTETra")
                .dailyQueueNumber(order.getDailyQueueNumber())
                .status("COMPLETED")
                .statusStep(4)
                .statusTitle("Giao Hàng Thành Công")
                .statusMessage("Đơn hàng đã được giao tận tay. Chúc quý khách thưởng trà an nhiên và thư thái!")
                .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                .build();

        sendToCustomer(order.getOrderCode(), event);
    }

    @Override
    public void broadcastOrderCancelled(Order order, String reason) {
        if (order == null) return;

        OrderStatusEventDto event = OrderStatusEventDto.builder()
                .eventType("CANCELLED")
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .branchId(order.getBranch() != null ? order.getBranch().getId() : 1L)
                .status("CANCELLED")
                .statusStep(-1)
                .statusTitle("Đơn Hàng Đã Bị Hủy")
                .statusMessage(reason != null ? reason : "Đơn hàng đã được hủy theo yêu cầu.")
                .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                .build();

        sendToCustomer(order.getOrderCode(), event);
    }


    private void sendToCustomer(String orderCode, OrderStatusEventDto event) {
        try {
            messagingTemplate.convertAndSend("/topic/order/" + orderCode, event);
            log.info(">>> [WebSocket] Phát sự kiện {} tới /topic/order/{}", event.getEventType(), orderCode);
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket tới /topic/order/{}: {}", orderCode, e.getMessage());
        }
    }

    private void sendToKitchen(Long branchId, OrderStatusEventDto event) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "NEW_ORDER_PAID");
            payload.put("eventType", event.getEventType());
            payload.put("branchId", branchId);
            payload.put("orderId", event.getOrderId());
            payload.put("orderCode", event.getOrderCode());
            payload.put("queueNumber", event.getDailyQueueNumber() != null ? event.getDailyQueueNumber() : 1);
            payload.put("status", event.getStatus());
            payload.put("timestamp", event.getTimestamp());
            messagingTemplate.convertAndSend("/topic/kitchen/" + branchId, (Object) payload);
            log.info(">>> [WebSocket] Phát sự kiện {} tới /topic/kitchen/{}", event.getEventType(), branchId);
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket tới /topic/kitchen/{}: {}", branchId, e.getMessage());
        }
    }

    private void sendToHandover(Long branchId, OrderStatusEventDto event) {
        try {
            messagingTemplate.convertAndSend("/topic/branch/" + branchId + "/handover", event);
            log.info(">>> [WebSocket] Phát sự kiện {} tới /topic/branch/{}/handover", event.getEventType(), branchId);
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket tới /topic/branch/{}/handover: {}", branchId, e.getMessage());
        }
    }

    private void sendToShipper(Long driverId, OrderStatusEventDto event) {
        try {
            messagingTemplate.convertAndSend("/topic/shipper/" + driverId, event);
            log.info(">>> [WebSocket] Phát sự kiện {} tới /topic/shipper/{}", event.getEventType(), driverId);
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket tới /topic/shipper/{}: {}", driverId, e.getMessage());
        }
    }
}
