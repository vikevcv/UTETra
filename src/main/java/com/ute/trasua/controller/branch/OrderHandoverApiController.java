package com.ute.trasua.controller.branch;

import com.ute.trasua.dto.handover.HandoverActionRequest;
import com.ute.trasua.dto.handover.HandoverOrderDto;
import com.ute.trasua.dto.handover.HandoverSlipDto;
import com.ute.trasua.entity.Driver;
import com.ute.trasua.repository.DriverRepository;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.service.OrderHandoverService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller REST API cho nghiệp vụ Bàn Giao Quầy Chi Nhánh (Module 8 - UC09, UC18).
 */
@RestController
@RequestMapping("/api/handover")
@RequiredArgsConstructor
@Slf4j
public class OrderHandoverApiController {

    private final OrderHandoverService orderHandoverService;
    private final DriverRepository driverRepository;
    private final OrderRepository orderRepository;

    /**
     * Lấy danh sách các đơn hàng chờ bàn giao tại chi nhánh.
     */
    @GetMapping("/orders")
    public ResponseEntity<List<HandoverOrderDto>> getPendingHandovers(
            @RequestParam(value = "branchId", required = false, defaultValue = "1") Long branchId,
            HttpServletRequest request) {
        if (!isPosBranchAllowed(request, branchId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        log.debug("API: Lấy danh sách đơn chờ bàn giao chi nhánh ID: {}", branchId);
        List<HandoverOrderDto> orders = orderHandoverService.getPendingHandovers(branchId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Bàn giao đơn hàng giao tận nơi cho tài xế giao hàng.
     */
    @PostMapping("/orders/{orderId}/driver")
    public ResponseEntity<HandoverOrderDto> handoverToDriver(
            @PathVariable Long orderId,
            @RequestBody(required = false) HandoverActionRequest request,
            HttpServletRequest httpRequest) {
        if (!isOrderInPosBranch(httpRequest, orderId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        log.info("API: Bàn giao đơn {} cho tài xế, ghi chú: {}",
                orderId, request != null ? request.getNotes() : "");
        Long driverId = (request != null) ? request.getDriverId() : null;
        String notes = (request != null) ? request.getNotes() : null;
        HandoverOrderDto result = orderHandoverService.handoverToDriver(orderId, driverId, notes);
        return ResponseEntity.ok(result);
    }

    /**
     * Bàn giao đơn hàng trực tiếp cho khách hàng tại quầy.
     */
    @PostMapping("/orders/{orderId}/customer")
    public ResponseEntity<HandoverOrderDto> handoverToCustomer(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {
        if (!isOrderInPosBranch(httpRequest, orderId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        log.info("API: Bàn giao đơn {} trực tiếp cho khách hàng", orderId);
        HandoverOrderDto result = orderHandoverService.handoverToCustomer(orderId);
        return ResponseEntity.ok(result);
    }

    /**
     * Lấy dữ liệu in phiếu bàn giao giao hàng theo biểu mẫu QLGH_BM1.
     */
    @GetMapping("/orders/{orderId}/slip")
    public ResponseEntity<HandoverSlipDto> getHandoverSlip(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {
        if (!isOrderInPosBranch(httpRequest, orderId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        log.debug("API: Lấy dữ liệu in phiếu bàn giao cho đơn hàng ID: {}", orderId);
        HandoverSlipDto slip = orderHandoverService.getHandoverSlipData(orderId);
        return ResponseEntity.ok(slip);
    }

    /**
     * Lấy danh sách tài xế chi nhánh đang bật ca trực (ONLINE).
     * Trả về danh sách rút gọn {id, fullName, phone, licensePlate, workingStatus}.
     */
    @GetMapping("/active-drivers")
    public ResponseEntity<List<Map<String, Object>>> getActiveDrivers(
            @RequestParam(value = "branchId", required = false, defaultValue = "1") Long branchId,
            HttpServletRequest request) {
        if (!isPosBranchAllowed(request, branchId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        log.debug("API: Lấy danh sách tài xế trực tuyến chi nhánh ID: {}", branchId);
        List<Driver> drivers = driverRepository.findByBranchIdWithUser(branchId);

        List<Map<String, Object>> activeDrivers = drivers.stream()
                .filter(d -> d.getWorkingStatus() == null || "ONLINE".equalsIgnoreCase(d.getWorkingStatus()))
                .map(d -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", d.getId());
                    map.put("fullName", (d.getUser() != null && d.getUser().getFullName() != null)
                            ? d.getUser().getFullName() : "Tài xế #" + d.getId());
                    map.put("phone", (d.getUser() != null && d.getUser().getPhone() != null)
                            ? d.getUser().getPhone() : "");
                    map.put("licensePlate", d.getLicensePlate() != null ? d.getLicensePlate() : "");
                    map.put("workingStatus", d.getWorkingStatus() != null ? d.getWorkingStatus() : "ONLINE");
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(activeDrivers);
    }

    private boolean isPosBranchAllowed(HttpServletRequest request, Long branchId) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("POS_ACCOUNT") == null) {
            return true;
        }
        Object sessionBranch = session.getAttribute("POS_BRANCH_ID");
        if (sessionBranch == null) {
            return false;
        }
        return branchId == null || sessionBranch.equals(branchId);
    }

    private boolean isOrderInPosBranch(HttpServletRequest request, Long orderId) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("POS_ACCOUNT") == null) {
            return true;
        }
        Object sessionBranch = session.getAttribute("POS_BRANCH_ID");
        if (sessionBranch == null) {
            return false;
        }
        return orderRepository.findById(orderId)
                .map(o -> o.getBranch() != null && sessionBranch.equals(o.getBranch().getId()))
                .orElse(false);
    }
}
