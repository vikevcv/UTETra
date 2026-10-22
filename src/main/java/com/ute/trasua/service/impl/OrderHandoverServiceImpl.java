package com.ute.trasua.service.impl;

import com.ute.trasua.util.NoteText;

import com.ute.trasua.dto.handover.HandoverActionRequest;
import com.ute.trasua.dto.handover.HandoverOrderDto;
import com.ute.trasua.dto.handover.HandoverSlipDto;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.Driver;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.OrderDetail;
import com.ute.trasua.entity.OrderDetailTopping;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.repository.DriverRepository;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.service.CloudinaryService;
import com.ute.trasua.service.OrderHandoverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Triển khai dịch vụ quản lý bàn giao và điều phối đơn hàng (Module 8 - UC09, UC18).
 */
@Service
@Slf4j
public class OrderHandoverServiceImpl implements OrderHandoverService {

    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final BranchRepository branchRepository;
    private final com.ute.trasua.service.OrderWebSocketPublisher orderWebSocketPublisher;
    private final com.ute.trasua.service.DriverDispatchService driverDispatchService;
    private final CloudinaryService cloudinaryService;

    public OrderHandoverServiceImpl(
            OrderRepository orderRepository,
            DriverRepository driverRepository,
            BranchRepository branchRepository,
            com.ute.trasua.service.OrderWebSocketPublisher orderWebSocketPublisher,
            com.ute.trasua.service.DriverDispatchService driverDispatchService,
            @org.springframework.beans.factory.annotation.Autowired(required = false) CloudinaryService cloudinaryService
    ) {
        super();
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
        this.branchRepository = branchRepository;
        this.orderWebSocketPublisher = orderWebSocketPublisher;
        this.driverDispatchService = driverDispatchService;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HandoverOrderDto> getPendingHandovers(Long branchId) {
        Long targetBranchId = (branchId != null) ? branchId : 1L;

        List<String> readyStatuses = List.of("READY_FOR_DELIVERY", "READY_FOR_PICKUP");
        List<Order> orders = orderRepository.findByBranchIdAndStatusInOrderByCreatedAtAsc(targetBranchId, readyStatuses);

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

        String type = (order.getOrderType() != null) ? order.getOrderType().toUpperCase() : "";
        if (!type.startsWith("POS") && !"PICKUP".equalsIgnoreCase(type)) {
            throw new IllegalStateException("Delivery orders cannot be handed over directly at the counter!");
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

    @Override
    @Transactional
    public HandoverOrderDto confirmDriverPickedUp(Long orderId, Long driverId, com.ute.trasua.dto.handover.DeliveryActionRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        assertDriverOwnershipAndBranch(order, driverId);

        String currentStatus = (order.getStatus() != null) ? order.getStatus().toUpperCase() : "";
        if (!"OUT_FOR_DELIVERY".equals(currentStatus) && !"READY_FOR_DELIVERY".equals(currentStatus) && !"DELIVERING".equals(currentStatus)) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái sẵn sàng lấy hàng (hiện tại: " + order.getStatus() + ")");
        }

        if (request == null) {
            request = new com.ute.trasua.dto.handover.DeliveryActionRequest();
        }
        validateProofPhoto(request.getPhotoData());

        String photoUrl = (cloudinaryService != null)
                ? cloudinaryService.uploadBase64Image(request.getPhotoData(), "deliveries/pickup")
                : request.getPhotoData();

        order.setPickupPhotoUrl(photoUrl);
        order.setStatus("DELIVERING");

        Driver driver = order.getDriver();
        if (driver != null) {
            driver.setWorkingStatus("DELIVERING");
            driverRepository.save(driver);
        }

        Order saved = orderRepository.save(order);
        log.info(">>> Tài xế #{} đã lấy hàng thành công đơn {}. Đang di chuyển giao.", driverId, saved.getOrderCode());

        try {
            if (orderWebSocketPublisher != null) {
                orderWebSocketPublisher.broadcastOrderDelivering(saved, driver);
            }
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket broadcastOrderDelivering: {}", e.getMessage());
        }

        return mapToHandoverOrderDto(saved);
    }

    @Override
    @Transactional
    public HandoverOrderDto confirmArrivedAtCustomer(Long orderId, Long driverId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        assertDriverOwnershipAndBranch(order, driverId);

        String currentStatus = (order.getStatus() != null) ? order.getStatus().toUpperCase() : "";
        if ("ARRIVED".equals(currentStatus)) {
            return mapToHandoverOrderDto(order); // Idempotent
        }
        if (!"DELIVERING".equals(currentStatus)) {
            throw new IllegalStateException("Đơn hàng phải ở trạng thái Đang giao hàng trước khi xác nhận đã tới điểm giao!");
        }

        order.setStatus("ARRIVED");
        Order saved = orderRepository.save(order);
        log.info(">>> Tài xế #{} đã tới điểm giao đơn {}", driverId, saved.getOrderCode());

        try {
            if (orderWebSocketPublisher != null) {
                orderWebSocketPublisher.broadcastOrderArrived(saved, saved.getDriver());
            }
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket khi tài xế tới điểm giao: {}", e.getMessage());
        }

        return mapToHandoverOrderDto(saved);
    }

    @Override
    @Transactional
    public HandoverOrderDto confirmDeliverySuccess(Long orderId, Long driverId, com.ute.trasua.dto.handover.DeliveryActionRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        assertDriverOwnershipAndBranch(order, driverId);

        String currentStatus = (order.getStatus() != null) ? order.getStatus().toUpperCase() : "";
        if ("COMPLETED".equals(currentStatus)) {
            return mapToHandoverOrderDto(order); // Idempotent
        }
        if (!"ARRIVED".equals(currentStatus)) {
            throw new IllegalStateException("Đơn hàng chưa ở trạng thái Đã tới điểm giao, không thể hoàn tất.");
        }

        if (request == null) {
            request = new com.ute.trasua.dto.handover.DeliveryActionRequest();
        }
        validateProofPhoto(request.getPhotoData());

        String photoUrl = (cloudinaryService != null)
                ? cloudinaryService.uploadBase64Image(request.getPhotoData(), "deliveries/delivery")
                : request.getPhotoData();

        order.setDeliveryPhotoUrl(photoUrl);
        order.setStatus("COMPLETED");
        order.setDeliveredAt(LocalDateTime.now());

        Driver driver = order.getDriver();
        if (driver != null) {
            int currentCompleted = (driver.getCompletedOrders() != null) ? driver.getCompletedOrders() : 0;
            driver.setCompletedOrders(currentCompleted + 1);

            List<Order> remaining = orderRepository.findByDriverIdAndStatusInOrderByCreatedAtDesc(
                    driver.getId(), List.of("OUT_FOR_DELIVERY", "DELIVERING", "ARRIVED"));
            boolean hasOtherActive = remaining.stream().anyMatch(o -> !o.getId().equals(order.getId()));
            driver.setWorkingStatus(hasOtherActive ? "DELIVERING" : "ONLINE");
            driverRepository.save(driver);
        }

        Order saved = orderRepository.save(order);
        log.info(">>> GIAO HÀNG THÀNH CÔNG: Đơn {} hoàn tất bởi tài xế #{}", saved.getOrderCode(), driverId);

        try {
            if (orderWebSocketPublisher != null) {
                orderWebSocketPublisher.broadcastOrderCompleted(saved);
            }
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket broadcastOrderCompleted: {}", e.getMessage());
        }

        return mapToHandoverOrderDto(saved);
    }

    @Override
    @Transactional
    public HandoverOrderDto reportDeliveryFailure(Long orderId, Long driverId, com.ute.trasua.dto.handover.DeliveryFailureRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        assertDriverOwnershipAndBranch(order, driverId);

        String currentStatus = (order.getStatus() != null) ? order.getStatus().toUpperCase() : "";
        if ("CANCELLED".equals(currentStatus)) {
            return mapToHandoverOrderDto(order); // Idempotent
        }
        // Chỉ đơn đang trong luồng giao vận mới được báo sự cố.
        // Chặn COMPLETED: nếu không, một đơn đã giao xong bị gửi lại /failure sẽ bị đổi sang CANCELLED,
        // làm sai doanh thu và sinh thêm đơn pha lại 0đ.
        if (!List.of("READY_FOR_DELIVERY", "OUT_FOR_DELIVERY", "DELIVERING", "ARRIVED").contains(currentStatus)) {
            throw new IllegalStateException("Đơn " + order.getOrderCode() + " đang ở trạng thái " + currentStatus
                    + " — không thể báo sự cố giao hàng!");
        }

        Driver reportingDriver = (driverId != null) ? driverRepository.findById(driverId).orElse(null) : null;

        String reasonCode = (request != null && request.getReasonCode() != null && !request.getReasonCode().isBlank())
                ? request.getReasonCode().trim()
                : "KHONG_LIEN_LAC_DUOC";

        boolean isBeforePickup = ("OUT_FOR_DELIVERY".equals(currentStatus) || "READY_FOR_DELIVERY".equals(currentStatus))
                && order.getPickupPhotoUrl() == null;

        boolean isStoreIncident = isBeforePickup && ("XE_HONG".equals(reasonCode) || "SUC_KHOE".equals(reasonCode));
        boolean isBreakdown = "XE_HONG".equals(reasonCode) || "SUC_KHOE".equals(reasonCode);

        // Ảnh chụp đối soát:
        // - Khi báo sự cố tại quán trước khi lấy trà (trả đơn lại cho quầy): KHÔNG bắt buộc ảnh chụp
        // - Khi đang đi giao ngoài đường mà bị thất bại: Bắt buộc ảnh chụp đối soát
        if (!isStoreIncident) {
            if (request == null || request.getProofPhotoData() == null || request.getProofPhotoData().isBlank()) {
                throw new IllegalArgumentException(
                        "Vui lòng chụp ảnh đối soát (ly trà / hiện trường) trước khi báo giao thất bại!");
            }
            validateProofPhoto(request.getProofPhotoData());
        } else if (request != null && request.getProofPhotoData() != null && !request.getProofPhotoData().isBlank()) {
            validateProofPhoto(request.getProofPhotoData());
        }

        String proofUrl = null;
        if (request != null && request.getProofPhotoData() != null && !request.getProofPhotoData().isBlank()) {
            proofUrl = (cloudinaryService != null)
                    ? cloudinaryService.uploadBase64Image(request.getProofPhotoData(), "deliveries/failure_proof")
                    : request.getProofPhotoData();
        }

        String note = (request != null && request.getNote() != null) ? request.getNote().trim() : "";
        String reasonVi = formatFailureReasonVietnamese(reasonCode);
        String failureSummary = reasonVi + (!note.isBlank() ? ": " + note : "");

        if (isBeforePickup && ("XE_HONG".equals(reasonCode) || "SUC_KHOE".equals(reasonCode))) {
            // Sự cố cá nhân / xe cộ tại quán trước khi lấy trà:
            // Trả TOÀN BỘ các đơn chưa lấy trà trong ca của tài xế này về hàng đợi điều phối của chi nhánh
            List<Order> unpickedOrders = (reportingDriver != null)
                    ? new ArrayList<>(orderRepository.findByDriverIdAndStatusInOrderByCreatedAtDesc(
                            reportingDriver.getId(), List.of("OUT_FOR_DELIVERY", "READY_FOR_DELIVERY")))
                    : new ArrayList<>(List.of(order));

            if (!unpickedOrders.contains(order)) {
                unpickedOrders.add(order);
            }

            // BẮT BUỘC: chuyển tài xế sang OFFLINE và FLUSH xuống DB TRƯỚC khi điều phối lại.
            // Nếu để sau vòng lặp, lockEligibleDriversForBranch() vẫn thấy anh ta ONLINE
            // và trả đơn về lại chính tài xế vừa báo sự cố -> đơn kẹt trên người đã nghỉ ca.
            if (reportingDriver != null) {
                reportingDriver.setWorkingStatus("OFFLINE");
                driverRepository.saveAndFlush(reportingDriver);
            }

            for (Order o : unpickedOrders) {
                // Chỉ trả những đơn chưa chụp ảnh lấy trà
                if (o.getPickupPhotoUrl() == null) {
                    o.setStatus("READY_FOR_DELIVERY");
                    o.setDriver(null);
                    o.setIncidentReason(reasonCode);
                    o.setIncidentNote(NoteText.truncate(failureSummary, NoteText.INCIDENT_NOTE_MAX));
                    o.setIncidentReportedAt(LocalDateTime.now());
                    String existingNotes = o.getDeliveryNotes();
                    if (existingNotes != null && !existingNotes.isBlank()) {
                        o.setDeliveryNotes(NoteText.append(existingNotes, failureSummary,
                                NoteText.DELIVERY_NOTES_MAX));
                    } else {
                        o.setDeliveryNotes(NoteText.append(null, failureSummary,
                                NoteText.DELIVERY_NOTES_MAX));
                    }
                    Order savedOrder = orderRepository.saveAndFlush(o);

                    // Thông báo WebSocket & tự động chuyển đơn cho tài xế khác nếu có
                    try {
                        if (orderWebSocketPublisher != null) {
                            orderWebSocketPublisher.broadcastDriverIncident(savedOrder, reportingDriver, reasonVi, note);
                        }
                        if (driverDispatchService != null) {
                            driverDispatchService.reassignCancelledOrder(savedOrder.getId());
                        }
                    } catch (Exception ex) {
                        log.warn("Lỗi thông báo khi trả đơn {}: {}", savedOrder.getOrderCode(), ex.getMessage());
                    }
                }
            }

            // Tài xế gặp sự cố xe/sức khỏe đã được chuyển OFFLINE trước vòng lặp điều phối lại
            log.info(">>> Tài xế gặp sự cố tại quán ({}), đã trả toàn bộ {} đơn chưa lấy trà về hàng đợi điều phối",
                    reasonCode, unpickedOrders.size());
            return mapToHandoverOrderDto(order);
        } else {
            // Đã nhận trà mang đi hoặc quán báo hủy món: Tự động hủy đơn hàng hoàn (CANCELLED)
            order.setFailureReason(reasonCode);
            order.setFailureProofUrl(proofUrl);
            order.setReturnedAt(LocalDateTime.now());
            order.setStatus("CANCELLED");

            // Hỏng xe / sức khỏe: tài xế nghỉ ca hẳn. Ngược lại chỉ giải phóng khi không còn đơn khác đang giao
            if (reportingDriver != null) {
                if (isBreakdown) {
                    reportingDriver.setWorkingStatus("OFFLINE");
                } else {
                    List<Order> remaining = orderRepository.findByDriverIdAndStatusInOrderByCreatedAtDesc(
                            reportingDriver.getId(), List.of("OUT_FOR_DELIVERY", "DELIVERING", "ARRIVED"));
                    boolean hasOtherActive = remaining.stream().anyMatch(o -> !o.getId().equals(order.getId()));
                    reportingDriver.setWorkingStatus(hasOtherActive ? "DELIVERING" : "ONLINE");
                }
                driverRepository.saveAndFlush(reportingDriver);
            }

            log.info(">>> Đã ghi nhận giao hàng thất bại & tự động hủy đơn {} (Lý do: {})", order.getOrderCode(), reasonCode);
        }

        String existingNotes = order.getDeliveryNotes();
        if (existingNotes != null && !existingNotes.isBlank()) {
            order.setDeliveryNotes(NoteText.append(existingNotes, failureSummary,
                    NoteText.DELIVERY_NOTES_MAX));
        } else {
            order.setDeliveryNotes(NoteText.append(null, failureSummary,
                    NoteText.DELIVERY_NOTES_MAX));
        }

        Order saved = orderRepository.saveAndFlush(order);

        // Sự cố hỏng xe / sức khỏe khi ĐÃ lấy trà: tài xế nghỉ ca, trả các đơn CHƯA LẤY TRÀ của anh ta
        // về hàng đợi ghép tài xế khác (ly của các đơn đó vẫn còn nguyên ở quán).
        if (isBreakdown && !isStoreIncident && reportingDriver != null) {
            releaseUnpickedOrdersOfDriver(reportingDriver, saved.getId(), reasonCode, failureSummary);
        }

        // Đơn tài xế đang cầm trên tay không cứu được nữa -> quán pha lại đơn MỚI, khách không trả tiền lần hai.
        if (isBreakdown && !isStoreIncident) {
            Order remake = createRemakeOrder(saved, reasonCode);
            if (remake != null) {
                String notes = saved.getDeliveryNotes();
                String trail = "Đã pha lại: " + remake.getOrderCode();
                saved.setDeliveryNotes((notes != null && !notes.isBlank()) ? notes + " | " + trail : trail);
                orderRepository.save(saved);
            }
        }

        // Phát WebSocket & Điều phối lại tự động cho tài xế khác nếu ở Trường hợp 1
        try {
            if ("READY_FOR_DELIVERY".equals(saved.getStatus())) {
                if (orderWebSocketPublisher != null) {
                    orderWebSocketPublisher.broadcastDriverIncident(saved, reportingDriver, reasonVi, note);
                }

                // TỰ ĐỘNG CHUYỂN ĐƠN CHO TÀI XẾ KHÁC
                if (driverDispatchService != null) {
                    com.ute.trasua.dto.dispatch.DispatchResultDto dispatchResult = driverDispatchService.reassignCancelledOrder(saved.getId());
                    if (dispatchResult != null && Boolean.TRUE.equals(dispatchResult.getReassigned())) {
                        log.info(">>> Tự động chuyển đơn {} cho tài xế khác thành công: {}", saved.getOrderCode(), dispatchResult.getMessage());
                        saved = orderRepository.findById(saved.getId()).orElse(saved);
                    } else {
                        log.info(">>> Chưa có tài xế trực ca rảnh thay thế, đơn {} nằm tại hàng đợi chi nhánh: {}",
                                saved.getOrderCode(), (dispatchResult != null ? dispatchResult.getMessage() : "Hàng đợi"));
                        if (orderWebSocketPublisher != null) {
                            orderWebSocketPublisher.broadcastOrderPrepared(saved);
                        }
                    }
                }
            } else {
                if (orderWebSocketPublisher != null) {
                    orderWebSocketPublisher.broadcastOrderCancelled(saved, failureSummary);
                }
            }
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket hoặc điều phối lại trong reportDeliveryFailure: {}", e.getMessage());
        }

        return mapToHandoverOrderDto(saved);
    }

    /**
     * Sự cố trên đường (hỏng xe / sức khỏe) khi tài xế đã cầm ly: quán pha lại một đơn MỚI
     * và đẩy vào hàng đợi KDS, KHÔNG thu tiền lần hai.
     * <ul>
     *   <li>Giữ nguyên STT trong ngày để khách nhận đúng số cũ.</li>
     *   <li>Tổng tiền = 0 để không đội doanh thu (đơn gốc đã ghi nhận tiền).</li>
     *   <li>Mã đơn = {@code <mã gốc>-R1}, ghi chú nêu rõ đơn gốc để barista đối soát.</li>
     * </ul>
     */
    private Order createRemakeOrder(Order original, String reasonCode) {
        if (original.getOrderDetails() == null || original.getOrderDetails().isEmpty()) {
            return null;
        }

        String baseCode = (original.getOrderCode() != null) ? original.getOrderCode() : "ORD";
        String remakeCode = baseCode + "-R1";
        if (remakeCode.length() > 50) {
            remakeCode = remakeCode.substring(0, 50);
        }
        if (orderRepository.findByOrderCode(remakeCode).isPresent()) {
            // ponytail: chống bấm/gọi trùng tạo 2 đơn pha lại, chỉ cần 1 bản ghi là đủ
            return null;
        }

        String reasonLabel = "XE_HONG".equals(reasonCode) ? "hỏng xe" : "sức khỏe tài xế";

        Order remake = Order.builder()
                .orderCode(remakeCode)
                .dailyQueueNumber(original.getDailyQueueNumber())
                .orderType(original.getOrderType())
                .status("PENDING")
                .paymentStatus("PAID") // tiền đã thu ở đơn gốc -> KDS nhận pha ngay
                .paymentMethod(original.getPaymentMethod())
                .paidAt(LocalDateTime.now())
                .cashierUsername(original.getCashierUsername())
                .branch(original.getBranch())
                .customerName(original.getCustomerName())
                .customerPhone(original.getCustomerPhone())
                .deliveryAddress(original.getDeliveryAddress())
                .latitude(original.getLatitude())
                .longitude(original.getLongitude())
                .distanceKm(original.getDistanceKm())
                .note("Pha lại do sự cố đơn " + baseCode + " (" + reasonLabel + ")")
                .totalAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .orderDetails(new ArrayList<>())
                .build();

        for (OrderDetail detail : original.getOrderDetails()) {
            OrderDetail copy = OrderDetail.builder()
                    .order(remake)
                    .product(detail.getProduct())
                    .productName(detail.getProductName())
                    .size(detail.getSize())
                    .sizeName(detail.getSizeName())
                    .quantity(detail.getQuantity())
                    .sugarLevel(detail.getSugarLevel())
                    .iceLevel(detail.getIceLevel())
                    .unitPrice(BigDecimal.ZERO)
                    .subtotal(BigDecimal.ZERO)
                    .note(detail.getNote())
                    .toppings(new ArrayList<>())
                    .build();

            if (detail.getToppings() != null) {
                for (OrderDetailTopping topping : detail.getToppings()) {
                    copy.getToppings().add(OrderDetailTopping.builder()
                            .orderDetail(copy)
                            .topping(topping.getTopping())
                            .toppingName(topping.getToppingName())
                            .price(BigDecimal.ZERO)
                            .build());
                }
            }
            remake.getOrderDetails().add(copy);
        }

        Order saved = orderRepository.saveAndFlush(remake);
        log.info(">>> [PHA LẠI] Đơn {} gặp sự cố ({}) -> đã tạo đơn pha lại {} (STT #{})",
                baseCode, reasonCode, saved.getOrderCode(), saved.getDailyQueueNumber());

        try {
            if (orderWebSocketPublisher != null) {
                orderWebSocketPublisher.broadcastOrderCreated(saved); // báo KDS + màn hình khách
            }
        } catch (Exception e) {
            log.warn("Lỗi phát WebSocket đơn pha lại {}: {}", saved.getOrderCode(), e.getMessage());
        }

        return saved;
    }

    /**
     * Tài xế gặp sự cố hỏng xe / sức khỏe: trả toàn bộ đơn CHƯA LẤY TRÀ trong ca của anh ta
     * về hàng đợi ghép tài xế khác, rồi thử ghép ngay cho tài xế đang trực ca.
     */
    private void releaseUnpickedOrdersOfDriver(Driver driver, Long currentOrderId, String reasonCode, String failureSummary) {
        List<Order> others = orderRepository.findByDriverIdAndStatusInOrderByCreatedAtDesc(
                driver.getId(), List.of("OUT_FOR_DELIVERY", "READY_FOR_DELIVERY"));

        for (Order other : others) {
            if (other.getId().equals(currentOrderId) || other.getPickupPhotoUrl() != null) {
                continue;
            }

            other.setStatus("READY_FOR_DELIVERY");
            other.setDriver(null);
            other.setIncidentReason(reasonCode);
            other.setIncidentNote(NoteText.truncate(failureSummary, NoteText.INCIDENT_NOTE_MAX));
            other.setIncidentReportedAt(LocalDateTime.now());
            Order savedOther = orderRepository.saveAndFlush(other);

            log.info(">>> [SỰ CỐ TÀI XẾ] Trả đơn {} (chưa lấy trà) của tài xế #{} về hàng đợi ghép",
                    savedOther.getOrderCode(), driver.getId());

            try {
                if (orderWebSocketPublisher != null) {
                    orderWebSocketPublisher.broadcastDriverIncident(savedOther, driver, failureSummary, null);
                }
                if (driverDispatchService != null) {
                    driverDispatchService.reassignCancelledOrder(savedOther.getId());
                }
            } catch (Exception ex) {
                log.warn("Lỗi trả đơn {} về hàng đợi ghép: {}", savedOther.getOrderCode(), ex.getMessage());
            }
        }
    }

    private void validateProofPhoto(String photoData) {
        if (photoData == null || photoData.isBlank() || photoData.length() < 100) {
            throw new IllegalArgumentException(
                    "Vui lòng chụp ảnh đối soát (ly trà / hiện trường) trước khi báo giao thất bại!");
        }
        if (photoData.length() > 7_000_000) {
            throw new IllegalArgumentException("Ảnh đối soát quá lớn (tối đa 5MB), vui lòng chụp lại!");
        }
    }

    private void assertDriverOwnershipAndBranch(Order order, Long driverId) {
        if (driverId == null) {
            throw new org.springframework.security.access.AccessDeniedException("Thiếu định danh tài xế thực hiện thao tác.");
        }
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Không tìm thấy thông tin tài xế ID: " + driverId));

        if (order.getDriver() == null || !driverId.equals(order.getDriver().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền thao tác trên đơn hàng được phân công cho tài xế khác.");
        }
        if (order.getBranch() != null && driver.getBranch() != null && !order.getBranch().getId().equals(driver.getBranch().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Đơn hàng không thuộc chi nhánh công tác của bạn.");
        }
    }

    private String formatFailureReasonVietnamese(String reasonCode) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return "Không liên lạc được với khách hàng";
        }
        switch (reasonCode.trim().toUpperCase()) {
            case "KHONG_LIEN_LAC_DUOC":
                return "Không liên lạc được với khách hàng";
            case "KHACH_TU_CHOI":
                return "Khách từ chối nhận trà / Đổi ý";
            case "SAI_DIA_CHI":
                return "Sai địa chỉ giao hàng";
            case "XE_HONG":
                return "Sự cố phương tiện giao thông trên đường";
            case "SUC_KHOE":
                return "Sự cố sức khỏe tài xế trên đường";
            case "QUAN_HET_MON":
                return "Quán tạm hết nguyên liệu pha chế";
            case "DO_VO_HU_HONG":
                return "Sự cố rơi đổ / hư hỏng trên đường giao";
            case "THOI_TIET_XAU":
                return "Thời tiết mưa bão nghiêm trọng";
            default:
                return reasonCode.replace("_", " ");
        }
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

        String rawSt = (order.getStatus() != null) ? order.getStatus().toUpperCase() : "PENDING";
        String statusLabel;
        switch (rawSt) {
            case "PREPARING": statusLabel = "Đang Pha Chế"; break;
            case "READY_FOR_DELIVERY": statusLabel = "Chờ Bàn Giao Shipper"; break;
            case "READY_FOR_PICKUP": statusLabel = "Sẵn Sàng Nhận Tại Quầy"; break;
            case "OUT_FOR_DELIVERY":
            case "DELIVERING": statusLabel = "Đang Giao Hàng"; break;
            case "ARRIVED": statusLabel = "Tài Xế Đã Đến Nơi"; break;
            case "PAID": statusLabel = "Đã Thanh Toán"; break;
            case "AWAITING_PREPARATION": statusLabel = "Chờ Pha Chế"; break;
            case "COMPLETED": statusLabel = "Giao Thành Công"; break;
            case "DELIVERY_FAILED":
            case "CANCELLED": statusLabel = "Đã Hủy / Sự Cố"; break;
            default: statusLabel = "Đã Tiếp Nhận"; break;
        }

        return HandoverOrderDto.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .dailyQueueNumber(order.getDailyQueueNumber() != null ? order.getDailyQueueNumber() : 1)
                .orderType(order.getOrderType() != null ? order.getOrderType() : "POS_WALK_IN")
                .status(order.getStatus())
                .statusLabel(statusLabel)
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
                .pickupPhotoUrl(order.getPickupPhotoUrl())
                .deliveryPhotoUrl(order.getDeliveryPhotoUrl())
                .failureReason(order.getFailureReason())
                .failureProofUrl(order.getFailureProofUrl())
                .returnedAt(order.getReturnedAt())
                .build();
    }
}
