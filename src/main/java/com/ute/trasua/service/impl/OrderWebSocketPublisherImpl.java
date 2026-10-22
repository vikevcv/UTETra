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
                .statusLabel("Đã Tiếp Nhận")
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
                .statusLabel("Đang Pha Chế")
                .statusStep(2)
                .statusTitle("Quầy Barista Đang Pha Chế")
                .statusMessage("Nghệ nhân pha chế đang thực hiện tùy biến ly trà theo đúng yêu cầu của bạn.")
                .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                .build();

        sendToCustomer(order.getOrderCode(), event);

        Long branchId = order.getBranch() != null ? order.getBranch().getId() : 1L;
        sendToKitchen(branchId, event);
    }

    @Override
    public void broadcastOrderPrepared(Order order) {
        if (order == null) return;

        String rawStatus = order.getStatus() != null ? order.getStatus() : "PREPARED";
        String label = "READY_FOR_DELIVERY".equalsIgnoreCase(rawStatus)
                ? "Chờ Bàn Giao Shipper"
                : ("READY_FOR_PICKUP".equalsIgnoreCase(rawStatus) ? "Sẵn Sàng Nhận Tại Quầy" : "Đã Pha Xong");

        OrderStatusEventDto event = OrderStatusEventDto.builder()
                .eventType("PREPARED")
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .branchId(order.getBranch() != null ? order.getBranch().getId() : 1L)
                .branchName(order.getBranch() != null ? order.getBranch().getName() : "Chi nhánh UTETra")
                .dailyQueueNumber(order.getDailyQueueNumber())
                .status(rawStatus)
                .statusLabel(label)
                .statusStep(2)
                .statusTitle("Pha Chế Hoàn Tất — " + label)
                .statusMessage("Ly trà đã được đóng gói kỹ lưỡng và chuyển sang bàn giao cho bộ phận giao nhận.")
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
                .status("OUT_FOR_DELIVERY")
                .statusLabel("Đang Giao Hàng")
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

        Long branchId = order.getBranch() != null ? order.getBranch().getId() : 1L;
        sendToHandover(branchId, event);

        if (driver != null) {
            sendToShipper(driver.getId(), event);
        }
    }

    @Override
    public void broadcastOrderDelivering(Order order, Driver driver) {
        if (order == null) return;

        String driverName = driver != null && driver.getUser() != null ? driver.getUser().getFullName() : "Tài xế UTETra";
        String driverPhone = driver != null && driver.getUser() != null ? driver.getUser().getPhone() : "";
        String vehiclePlate = driver != null ? driver.getLicensePlate() : "";

        OrderStatusEventDto event = OrderStatusEventDto.builder()
                .eventType("DELIVERING")
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .branchId(order.getBranch() != null ? order.getBranch().getId() : 1L)
                .branchName(order.getBranch() != null ? order.getBranch().getName() : "Chi nhánh UTETra")
                .dailyQueueNumber(order.getDailyQueueNumber())
                .status("DELIVERING")
                .statusLabel("Đang Giao Hàng")
                .statusStep(3)
                .statusTitle("Tài Xế Đang Di Chuyển Giao Trà")
                .statusMessage("Tài xế " + driverName + " đã xuất phát từ quán và đang trên đường tới bạn.")
                .driverId(driver != null ? driver.getId() : null)
                .driverName(driverName)
                .driverPhone(driverPhone)
                .vehiclePlate(vehiclePlate)
                .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                .build();

        sendToCustomer(order.getOrderCode(), event);

        Long branchId = order.getBranch() != null ? order.getBranch().getId() : 1L;
        sendToHandover(branchId, event);

        if (driver != null) {
            sendToShipper(driver.getId(), event);
        }
    }

    @Override
    public void broadcastOrderArrived(Order order, Driver driver) {
        if (order == null) return;

        String driverName = driver != null && driver.getUser() != null ? driver.getUser().getFullName() : "Tài xế UTETra";
        String driverPhone = driver != null && driver.getUser() != null ? driver.getUser().getPhone() : "";
        String vehiclePlate = driver != null ? driver.getLicensePlate() : "";

        OrderStatusEventDto event = OrderStatusEventDto.builder()
                .eventType("ARRIVED")
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .branchId(order.getBranch() != null ? order.getBranch().getId() : 1L)
                .branchName(order.getBranch() != null ? order.getBranch().getName() : "Chi nhánh UTETra")
                .dailyQueueNumber(order.getDailyQueueNumber())
                .status("ARRIVED")
                .statusLabel("Tài Xế Đã Đến Nơi")
                .statusStep(3)
                .statusTitle("Tài Xế Đã Đến Điểm Giao Hàng")
                .statusMessage("Tài xế " + driverName + " đã đến điểm giao. Quý khách vui lòng để ý điện thoại và ra nhận trà!")
                .driverId(driver != null ? driver.getId() : null)
                .driverName(driverName)
                .driverPhone(driverPhone)
                .vehiclePlate(vehiclePlate)
                .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                .build();

        sendToCustomer(order.getOrderCode(), event);

        Long branchId = order.getBranch() != null ? order.getBranch().getId() : 1L;
        sendToHandover(branchId, event);

        if (driver != null) {
            sendToShipper(driver.getId(), event);
        }
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
                .statusLabel("Giao Thành Công")
                .statusStep(4)
                .statusTitle("Giao Hàng Thành Công")
                .statusMessage("Đơn hàng đã được giao tận tay. Chúc quý khách thưởng trà an nhiên và thư thái!")
                .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                .build();

        sendToCustomer(order.getOrderCode(), event);

        Long branchId = order.getBranch() != null ? order.getBranch().getId() : 1L;
        sendToHandover(branchId, event);

        if (order.getDriver() != null) {
            sendToShipper(order.getDriver().getId(), event);
        }
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
                .statusLabel("Đã Hủy")
                .statusStep(-1)
                .statusTitle("Đơn Hàng Đã Bị Hủy")
                .statusMessage(reason != null ? reason : "Đơn hàng đã được hủy theo yêu cầu.")
                .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                .build();

        sendToCustomer(order.getOrderCode(), event);

        Long branchId = order.getBranch() != null ? order.getBranch().getId() : 1L;
        sendToKitchen(branchId, event);
        sendToHandover(branchId, event);

        if (order.getDriver() != null) {
            sendToShipper(order.getDriver().getId(), event);
        }
    }

    @Override
    public void broadcastDriverIncident(Order order, Driver driver, String reason, String note) {
        if (order == null) return;
        Long branchId = order.getBranch() != null ? order.getBranch().getId() : 1L;
        String driverName = (driver != null && driver.getUser() != null) ? driver.getUser().getFullName() : "Tài xế";

        String reasonDisplay = (reason != null && !reason.isBlank()) ? reason : "Sự cố bất khả kháng";
        String noteDisplay = (note != null && !note.isBlank()) ? ": " + note.trim() : "";
        String statusMsg = "Tài xế " + driverName + " báo cáo sự cố: " + reasonDisplay + noteDisplay;

        OrderStatusEventDto event = OrderStatusEventDto.builder()
                .eventType("DRIVER_INCIDENT")
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .branchId(branchId)
                .branchName(order.getBranch() != null ? order.getBranch().getName() : "Chi nhánh UTETra")
                .dailyQueueNumber(order.getDailyQueueNumber())
                .status(order.getStatus())
                .statusStep(-1)
                .statusTitle("Sự Cố Giao Hàng Bất Khả Kháng")
                .statusMessage(statusMsg)
                .driverId(driver != null ? driver.getId() : null)
                .driverName(driverName)
                .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                .build();

        sendToHandover(branchId, event);
        if (driver != null) {
            sendToShipper(driver.getId(), event);
        }
        sendToCustomer(order.getOrderCode(), event);
    }

    @Override
    public void broadcastQueueUnassigned(Long branchId, int unassignedCount, java.util.List<String> orderCodes) {
        OrderStatusEventDto event = OrderStatusEventDto.builder()
                .eventType("UNASSIGNED_QUEUE")
                .branchId(branchId != null ? branchId : 1L)
                .statusTitle("Cảnh Báo: Đơn Chờ Điều Phối")
                .statusMessage("Hiện có " + unassignedCount + " đơn hàng chưa được gán tài xế do hết tài xế khả dụng.")
                .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                .build();

        sendToHandover(branchId != null ? branchId : 1L, event);
    }

    @Override
    public void broadcastOnlineOrderPaid(Order order) {
        if (order == null) return;
        Long branchId = order.getBranch() != null ? order.getBranch().getId() : 1L;

        OrderStatusEventDto event = OrderStatusEventDto.builder()
                .eventType("PAYMENT_SUCCESS")
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .branchId(branchId)
                .branchName(order.getBranch() != null ? order.getBranch().getName() : "Chi nhánh UTETra")
                .dailyQueueNumber(order.getDailyQueueNumber())
                .status(order.getStatus())
                .statusLabel("Đã Thanh Toán — Chờ Pha Chế")
                .statusStep(1)
                .statusTitle("Thanh Toán Trực Tuyến Thành Công")
                .statusMessage("Đơn hàng đã được thanh toán 100% qua PayOS và đang chuyển đến quầy pha chế.")
                .timestamp(LocalDateTime.now().format(TIME_FORMATTER))
                .build();

        // 1. Gửi tới kênh riêng của khách hàng (/topic/order/{orderCode})
        sendToCustomer(order.getOrderCode(), event);

        // 2. Gửi tín hiệu thông báo có đơn mới tới KDS quầy pha chế Barista
        sendToKitchen(branchId, event);

        // 3. Gửi tín hiệu thông báo quầy POS (/topic/pos/{branchId})
        sendToPos(branchId, order, event);
    }

    private void sendToPos(Long branchId, Order order, OrderStatusEventDto event) {
        try {
            boolean isPosOrder = (order.getOrderCode() != null && order.getOrderCode().startsWith("ORD-POS-"))
                    || "POS_WALK_IN".equalsIgnoreCase(order.getOrderType());

            Map<String, Object> payload = new HashMap<>();
            payload.put("type", isPosOrder ? "POS_ORDER_PAID" : "ONLINE_ORDER_PAID");
            payload.put("paid", true);
            payload.put("isPosOrder", isPosOrder);
            payload.put("branchId", branchId);
            payload.put("orderId", order.getId());
            payload.put("orderCode", order.getOrderCode());
            payload.put("queueNumber", order.getDailyQueueNumber() != null ? order.getDailyQueueNumber() : 1);
            payload.put("customerName", order.getCustomerName());
            payload.put("customerPhone", order.getCustomerPhone());
            payload.put("finalAmount", order.getFinalAmount());
            payload.put("paymentMethod", order.getPaymentMethod());
            payload.put("timestamp", event.getTimestamp());
            messagingTemplate.convertAndSend("/topic/pos/" + branchId, (Object) payload);
            log.info(">>> [WebSocket] Phát sự kiện {} tới /topic/pos/{}", payload.get("type"), branchId);
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket tới /topic/pos/{}: {}", branchId, e.getMessage());
        }
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

    @Override
    public void broadcastDriverStatusChanged(Driver driver) {
        if (driver == null) return;
        Long branchId = (driver.getBranch() != null) ? driver.getBranch().getId() : 1L;
        String status = (driver.getWorkingStatus() != null) ? driver.getWorkingStatus() : "OFFLINE";
        boolean locked = (driver.getUser() != null && driver.getUser().isLocked());
        String fullName = (driver.getUser() != null && driver.getUser().getFullName() != null)
                ? driver.getUser().getFullName() : "Tài xế";

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "DRIVER_STATUS_CHANGED");
        payload.put("driverId", driver.getId());
        payload.put("workingStatus", status);
        payload.put("locked", locked);
        payload.put("branchId", branchId);
        payload.put("driverName", fullName);
        payload.put("licensePlate", driver.getLicensePlate() != null ? driver.getLicensePlate() : "");
        payload.put("timestamp", LocalDateTime.now().format(TIME_FORMATTER));

        // 1. Phát tới kênh Quản trị viên (/topic/drivers)
        try {
            messagingTemplate.convertAndSend("/topic/drivers", (Object) payload);
            log.info(">>> [WebSocket] Phát sự kiện thay đổi trạng thái tài xế {} ({}) tới /topic/drivers", driver.getId(), status);
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket tới /topic/drivers: {}", e.getMessage());
        }

        // 2. Phát tới kênh Chi nhánh (/topic/branch/{branchId}/drivers)
        try {
            messagingTemplate.convertAndSend("/topic/branch/" + branchId + "/drivers", (Object) payload);
            log.info(">>> [WebSocket] Phát sự kiện thay đổi trạng thái tài xế {} ({}) tới /topic/branch/{}/drivers", driver.getId(), status, branchId);
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket tới /topic/branch/{}/drivers: {}", branchId, e.getMessage());
        }
    }
}
