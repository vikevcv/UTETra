package com.ute.trasua.service.impl;

import com.ute.trasua.dto.dispatch.DispatchResultDto;
import com.ute.trasua.dto.handover.HandoverOrderDto;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.Driver;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.OrderDetail;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.DriverRepository;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.service.DispatchNotifier;
import com.ute.trasua.service.DriverDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Triển khai dịch vụ điều phối và phân công đơn hàng bắt buộc cho tài xế trực ca (Module 9 - UC12).
 * Đảm bảo:
 * - Không có cơ chế 30s đếm ngược từ chối tự do (tài xế nội bộ nhận lương ca của quán).
 * - Tối đa 3 đơn/ca cho mỗi tài xế (MAX_QUEUE_PER_DRIVER = 3).
 * - Phòng chống Race Condition bằng Pessimistic Lock (SELECT ... FOR UPDATE) và Optimistic Version.
 * - Phát WebSocket sau khi transaction commit (DispatchNotifier).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DriverDispatchServiceImpl implements DriverDispatchService {

    public static final int MAX_QUEUE_PER_DRIVER = 3;
    public static final List<String> ACTIVE_DELIVERY_STATUSES = List.of("OUT_FOR_DELIVERY", "DELIVERING", "ARRIVED");

    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final DispatchNotifier dispatchNotifier;

    @Override
    @Transactional
    public DispatchResultDto dispatchMandatoryOrder(Long orderId, Long driverId, String note) {
        // Khóa bi quan dòng Order chống xung đột gán chồng đa luồng
        Order order = orderRepository.findWithLockById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        String currentStatus = (order.getStatus() != null) ? order.getStatus().toUpperCase() : "";
        String orderType = (order.getOrderType() != null) ? order.getOrderType().toUpperCase() : "";
        if ("POS_WALK_IN".equals(orderType) || "PICKUP".equals(orderType)) {
            throw new IllegalStateException("Counter orders cannot be assigned to a driver!");
        }
        if (!"READY_FOR_DELIVERY".equals(currentStatus)) {
            throw new IllegalStateException("Order " + order.getOrderCode() + " is not ready for delivery (current: " + currentStatus + ")");
        }
        if (order.getDriver() != null) {
            throw new IllegalStateException("Đơn hàng " + order.getOrderCode() + " đã được phân công cho tài xế #" + order.getDriver().getId());
        }

        // Ràng buộc chi nhánh tuyệt đối
        Long orderBranchId = (order.getBranch() != null) ? order.getBranch().getId() : 1L;

        Driver driver;
        if (driverId == null || driverId <= 0) {
            // Tự động phân công theo thuật toán 3 tầng tối ưu (Ít tải nhất -> KPI cao nhất -> Hoàn thành nhiều nhất)
            List<Driver> candidates = driverRepository.lockEligibleDriversForBranch(orderBranchId);
            if (candidates.isEmpty()) {
                throw new IllegalStateException("Chi nhánh hiện không có tài xế nào đang BẬT CA TRỰC (ONLINE) để bàn giao đơn!");
            }
            List<Long> driverIds = candidates.stream().map(Driver::getId).collect(Collectors.toList());
            Map<Long, Long> loadByDriver = new HashMap<>();
            List<Object[]> loadRows = orderRepository.countActiveOrdersByDriverIds(driverIds, ACTIVE_DELIVERY_STATUSES);
            for (Object[] row : loadRows) {
                loadByDriver.put((Long) row[0], (Long) row[1]);
            }
            driver = selectBestDriver(candidates, loadByDriver);
            if (driver == null) {
                throw new IllegalStateException("Tất cả tài xế trực tuyến của chi nhánh đều đã đầy tải (tối đa 3 đơn/ca)!");
            }
        } else {
            // Khóa bi quan dòng tài xế chỉ định
            driver = driverRepository.findWithLockById(driverId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài xế ID: " + driverId));
            Long driverBranchId = (driver.getBranch() != null) ? driver.getBranch().getId() : 1L;
            if (!orderBranchId.equals(driverBranchId)) {
                throw new IllegalStateException("Lỗi chi nhánh: Tài xế thuộc chi nhánh #" + driverBranchId
                        + " không thể tiếp nhận đơn của chi nhánh #" + orderBranchId);
            }
        }

        // Kiểm tra tài khoản tài xế có bị khóa hay không
        User driverUser = driver.getUser();
        if (driverUser != null && driverUser.isLocked()) {
            throw new IllegalStateException("Tài khoản tài xế đang bị tạm ngưng nhận cuốc do vi phạm chính sách");
        }

        // Kiểm tra trạng thái hoạt động: phải là ONLINE hoặc DELIVERING
        String workingStatus = (driver.getWorkingStatus() != null) ? driver.getWorkingStatus().toUpperCase() : "OFFLINE";
        if (!"ONLINE".equals(workingStatus) && !"DELIVERING".equals(workingStatus)) {
            throw new IllegalStateException("Tài xế đang ở trạng thái " + workingStatus + " (không trực ca), không thể gán đơn!");
        }

        // Kiểm tra trần hàng đợi tối đa 3 đơn
        long currentLoad = orderRepository.countByDriverIdAndStatusIn(driver.getId(), ACTIVE_DELIVERY_STATUSES);
        if (currentLoad >= MAX_QUEUE_PER_DRIVER) {
            throw new IllegalStateException("Tài xế " + (driverUser != null ? driverUser.getFullName() : driverId)
                    + " đã đạt trần tối đa " + MAX_QUEUE_PER_DRIVER + " đơn trong ca trực!");
        }

        // Thực hiện gán đơn bắt buộc
        order.setDriver(driver);
        order.setStatus("OUT_FOR_DELIVERY");
        order.setHandedOverAt(LocalDateTime.now());

        if (note != null && !note.isBlank()) {
            String existingNotes = order.getDeliveryNotes();
            if (existingNotes != null && !existingNotes.isBlank()) {
                order.setDeliveryNotes(existingNotes + " | Điều phối: " + note.trim());
            } else {
                order.setDeliveryNotes("Điều phối: " + note.trim());
            }
        }

        // Cập nhật trạng thái tài xế sang DELIVERING
        driver.setWorkingStatus("DELIVERING");
        driverRepository.save(driver);

        Order saved = orderRepository.save(order);
        int newQueueSize = (int) currentLoad + 1;

        log.info(">>> [MANDATORY DISPATCH] Đã gán đơn {} (STT #{}) cho tài xế {} (Tải ca: {}/{})",
                saved.getOrderCode(), saved.getDailyQueueNumber(),
                driverUser != null ? driverUser.getFullName() : driver.getId(),
                newQueueSize, MAX_QUEUE_PER_DRIVER);

        // Bắn WebSocket thời gian thực SAU KHI commit transaction
        dispatchNotifier.notifyOrderDispatched(saved, driver);

        return DispatchResultDto.builder()
                .orderId(saved.getId())
                .orderCode(saved.getOrderCode())
                .orderStatus(saved.getStatus())
                .driverId(driver.getId())
                .driverName(driverUser != null ? driverUser.getFullName() : "Tài xế #" + driver.getId())
                .driverWorkingStatus(driver.getWorkingStatus())
                .driverQueueSize(newQueueSize)
                .reassigned(false)
                .message("Phân công bắt buộc đơn hàng thành công")
                .build();
    }

    @Override
    @Transactional
    public int autoDispatchReadyOrders(Long branchId) {
        Long targetBranchId = (branchId != null) ? branchId : 1L;
        List<Order> unassignedQueue = orderRepository.findUnassignedDeliveryQueue(targetBranchId);
        if (unassignedQueue.isEmpty()) {
            return 0;
        }

        // Khóa bi quan toàn bộ tài xế khả dụng của chi nhánh
        List<Driver> candidates = driverRepository.lockEligibleDriversForBranch(targetBranchId);
        if (candidates.isEmpty()) {
            log.warn(">>> [AUTO-DISPATCH] Chi nhánh {} có {} đơn chờ nhưng không có tài xế nào ONLINE",
                    targetBranchId, unassignedQueue.size());
            List<String> codes = unassignedQueue.stream().map(Order::getOrderCode).collect(Collectors.toList());
            dispatchNotifier.notifyQueueUnassigned(targetBranchId, unassignedQueue.size(), codes);
            return 0;
        }

        // Lấy trước map tải số đơn đang giao của các tài xế trong 1 query (tránh N+1)
        List<Long> driverIds = candidates.stream().map(Driver::getId).collect(Collectors.toList());
        Map<Long, Long> loadByDriver = new HashMap<>();
        List<Object[]> loadRows = orderRepository.countActiveOrdersByDriverIds(driverIds, ACTIVE_DELIVERY_STATUSES);
        for (Object[] row : loadRows) {
            loadByDriver.put((Long) row[0], (Long) row[1]);
        }

        int dispatchedCount = 0;
        for (Order order : unassignedQueue) {
            Driver bestDriver = selectBestDriver(candidates, loadByDriver);
            if (bestDriver == null) {
                // Toàn bộ tài xế đều đã chạm trần 3 đơn
                log.warn(">>> [AUTO-DISPATCH] Tất cả tài xế chi nhánh {} đều đã đạt trần {} đơn, tạm dừng gán",
                        targetBranchId, MAX_QUEUE_PER_DRIVER);
                break;
            }

            dispatchMandatoryOrder(order.getId(), bestDriver.getId(), "Tự động phân công khi hoàn tất pha chế");
            dispatchedCount++;

            // Cập nhật bộ đếm tải tạm thời
            long newLoad = loadByDriver.getOrDefault(bestDriver.getId(), 0L) + 1;
            loadByDriver.put(bestDriver.getId(), newLoad);

            if (newLoad >= MAX_QUEUE_PER_DRIVER) {
                candidates.removeIf(d -> d.getId().equals(bestDriver.getId()));
            }
        }

        // Nếu còn đơn chưa thể gán do thiếu tài xế
        if (dispatchedCount < unassignedQueue.size()) {
            int remaining = unassignedQueue.size() - dispatchedCount;
            List<String> remainingCodes = unassignedQueue.stream()
                    .skip(dispatchedCount)
                    .map(Order::getOrderCode)
                    .collect(Collectors.toList());
            dispatchNotifier.notifyQueueUnassigned(targetBranchId, remaining, remainingCodes);
        }

        return dispatchedCount;
    }

    @Override
    @Transactional
    public DispatchResultDto reassignCancelledOrder(Long orderId) {
        Order order = orderRepository.findWithLockById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        if (!"READY_FOR_DELIVERY".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Chỉ điều phối lại được các đơn hàng đang ở trạng thái READY_FOR_DELIVERY");
        }

        Long branchId = (order.getBranch() != null) ? order.getBranch().getId() : 1L;
        List<Driver> candidates = driverRepository.lockEligibleDriversForBranch(branchId);
        if (candidates.isEmpty()) {
            dispatchNotifier.notifyQueueUnassigned(branchId, 1, List.of(order.getOrderCode()));
            return DispatchResultDto.builder()
                    .orderId(order.getId())
                    .orderCode(order.getOrderCode())
                    .orderStatus(order.getStatus())
                    .reassigned(false)
                    .message("Chưa có tài xế trực ca khả dụng để phân công lại đơn")
                    .build();
        }

        List<Long> driverIds = candidates.stream().map(Driver::getId).collect(Collectors.toList());
        Map<Long, Long> loadByDriver = new HashMap<>();
        List<Object[]> loadRows = orderRepository.countActiveOrdersByDriverIds(driverIds, ACTIVE_DELIVERY_STATUSES);
        for (Object[] row : loadRows) {
            loadByDriver.put((Long) row[0], (Long) row[1]);
        }

        Driver bestDriver = selectBestDriver(candidates, loadByDriver);
        if (bestDriver == null) {
            dispatchNotifier.notifyQueueUnassigned(branchId, 1, List.of(order.getOrderCode()));
            return DispatchResultDto.builder()
                    .orderId(order.getId())
                    .orderCode(order.getOrderCode())
                    .orderStatus(order.getStatus())
                    .reassigned(false)
                    .message("Toàn bộ tài xế đều đã đạt trần 3 đơn trong ca trực")
                    .build();
        }

        DispatchResultDto result = dispatchMandatoryOrder(orderId, bestDriver.getId(), "Quản lý chi nhánh điều phối lại sau sự cố");
        result.setReassigned(true);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDispatchQueue(Long branchId) {
        Long targetBranchId = (branchId != null) ? branchId : 1L;
        List<Order> unassigned = orderRepository.findUnassignedDeliveryQueue(targetBranchId);
        List<Driver> drivers = driverRepository.findByBranchIdWithUser(targetBranchId);

        List<Long> driverIds = drivers.stream().map(Driver::getId).collect(Collectors.toList());
        Map<Long, Long> loadByDriver = new HashMap<>();
        if (!driverIds.isEmpty()) {
            List<Object[]> loadRows = orderRepository.countActiveOrdersByDriverIds(driverIds, ACTIVE_DELIVERY_STATUSES);
            for (Object[] row : loadRows) {
                loadByDriver.put((Long) row[0], (Long) row[1]);
            }
        }

        List<Map<String, Object>> driverInfoList = new ArrayList<>();
        for (Driver d : drivers) {
            User u = d.getUser();
            long load = loadByDriver.getOrDefault(d.getId(), 0L);
            Map<String, Object> dm = new HashMap<>();
            dm.put("driverId", d.getId());
            dm.put("fullName", u != null ? u.getFullName() : "Tài xế #" + d.getId());
            dm.put("phone", u != null ? u.getPhone() : "");
            dm.put("licensePlate", d.getLicensePlate());
            dm.put("workingStatus", d.getWorkingStatus());
            dm.put("activeLoad", load);
            dm.put("maxLoad", MAX_QUEUE_PER_DRIVER);
            dm.put("isEligible", !"OFFLINE".equalsIgnoreCase(d.getWorkingStatus()) && load < MAX_QUEUE_PER_DRIVER);
            driverInfoList.add(dm);
        }

        List<HandoverOrderDto> queueDto = unassigned.stream()
                .map(this::mapToQueueDto)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("branchId", targetBranchId);
        response.put("pendingCount", unassigned.size());
        response.put("unassignedOrders", queueDto);
        response.put("drivers", driverInfoList);
        return response;
    }

    /**
     * Thuật toán 3 tầng ưu tiên chuẩn SE:
     * 1) Ưu tiên tài xế đang rảnh tại quán (ONLINE, 0 đơn)
     * 2) Cân bằng tải: tài xế có số đơn đang giao ít hơn
     * 3) Tie-break ổn định: ID nhỏ hơn
     */
    private Driver selectBestDriver(List<Driver> candidates, Map<Long, Long> loadByDriver) {
        Driver best = null;
        for (Driver d : candidates) {
            long load = loadByDriver.getOrDefault(d.getId(), 0L);
            if (load >= MAX_QUEUE_PER_DRIVER) {
                continue;
            }
            if (best == null) {
                best = d;
                continue;
            }

            boolean dIsOnline = "ONLINE".equalsIgnoreCase(d.getWorkingStatus()) && load == 0;
            boolean bestIsOnline = "ONLINE".equalsIgnoreCase(best.getWorkingStatus()) && loadByDriver.getOrDefault(best.getId(), 0L) == 0;

            if (dIsOnline != bestIsOnline) {
                if (dIsOnline) {
                    best = d;
                }
                continue;
            }

            long bestLoad = loadByDriver.getOrDefault(best.getId(), 0L);
            if (load != bestLoad) {
                // Tầng 1: Ưu tiên tài xế có ít đơn hơn
                if (load < bestLoad) {
                    best = d;
                }
            } else {
                // Tầng 2: Cùng số đơn -> Ưu tiên tài xế có điểm KPI cao hơn
                double dKpi = (d.getKpiScore() != null) ? d.getKpiScore() : 5.0;
                double bestKpi = (best.getKpiScore() != null) ? best.getKpiScore() : 5.0;
                if (Double.compare(dKpi, bestKpi) != 0) {
                    if (dKpi > bestKpi) {
                        best = d;
                    }
                } else {
                    // Tầng 3: Cùng KPI -> Ưu tiên tài xế có tổng cuốc giao hoàn thành nhiều hơn
                    int dCompleted = (d.getCompletedOrders() != null) ? d.getCompletedOrders() : 0;
                    int bestCompleted = (best.getCompletedOrders() != null) ? best.getCompletedOrders() : 0;
                    if (dCompleted != bestCompleted) {
                        if (dCompleted > bestCompleted) {
                            best = d;
                        }
                    } else if (d.getId() < best.getId()) {
                        // Tie-break cuối cùng ổn định theo ID nhỏ hơn
                        best = d;
                    }
                }
            }
        }
        return best;
    }

    private HandoverOrderDto mapToQueueDto(Order order) {
        int totalCups = 0;
        List<String> items = new ArrayList<>();
        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                int q = (d.getQuantity() != null && d.getQuantity() > 0) ? d.getQuantity() : 1;
                totalCups += q;
                items.add(d.getProductName() + " x" + q);
            }
        }

        return HandoverOrderDto.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .dailyQueueNumber(order.getDailyQueueNumber() != null ? order.getDailyQueueNumber() : 1)
                .orderType(order.getOrderType() != null ? order.getOrderType() : "DELIVERY")
                .status(order.getStatus())
                .customerName(order.getCustomerName() != null ? order.getCustomerName() : "Khách hàng")
                .customerPhone(order.getCustomerPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .cupCount(totalCups > 0 ? totalCups : 1)
                .itemsSummary(String.join(", ", items))
                .createdAt(order.getCreatedAt())
                .incidentReason(order.getIncidentReason())
                .incidentNote(order.getIncidentNote())
                .incidentReportedAt(order.getIncidentReportedAt())
                .build();
    }
}
