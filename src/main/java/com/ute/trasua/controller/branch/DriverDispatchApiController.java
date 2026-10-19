package com.ute.trasua.controller.branch;

import com.ute.trasua.dto.dispatch.DispatchResultDto;
import com.ute.trasua.dto.dispatch.DriverAssignRequest;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.service.DriverDispatchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API điều phối và phân công giao vận chi nhánh (Module 9 - UC12).
 * Phục vụ Quản lý chi nhánh điều phối đơn hàng và theo dõi tài xế.
 */
@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
@Slf4j
public class DriverDispatchApiController {

    private final DriverDispatchService driverDispatchService;
    private final OrderRepository orderRepository;

    /**
     * Phân công bắt buộc một đơn hàng cho tài xế trực ca.
     */
    @PostMapping("/orders/{orderId}/assign")
    public ResponseEntity<DispatchResultDto> assignOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody DriverAssignRequest body,
            HttpServletRequest request,
            Authentication authentication) {
        if (!isDispatchScopeAllowed(authentication, request, orderId, true)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        log.info("Quản lý yêu cầu phân công đơn {} cho tài xế {}", orderId, body.getDriverId());
        DispatchResultDto result = driverDispatchService.dispatchMandatoryOrder(orderId, body.getDriverId(), body.getNote());
        return ResponseEntity.ok(result);
    }

    /**
     * Điều phối lại một đơn hàng vừa gặp sự cố cho tài xế khác.
     */
    @PostMapping("/orders/{orderId}/reassign")
    public ResponseEntity<DispatchResultDto> reassignOrder(
            @PathVariable Long orderId,
            HttpServletRequest request,
            Authentication authentication) {
        if (!isDispatchScopeAllowed(authentication, request, orderId, true)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        log.info("Quản lý yêu cầu điều phối lại đơn {}", orderId);
        DispatchResultDto result = driverDispatchService.reassignCancelledOrder(orderId);
        return ResponseEntity.ok(result);
    }

    /**
     * Kích hoạt tự động quét hàng đợi chi nhánh và phân công theo 3 tầng ưu tiên.
     */
    @PostMapping("/orders/auto-dispatch")
    public ResponseEntity<Map<String, Object>> autoDispatch(
            @RequestParam(value = "branchId", defaultValue = "1") Long branchId,
            HttpServletRequest request,
            Authentication authentication) {
        if (!isDispatchScopeAllowed(authentication, request, branchId, false)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        log.info("Kích hoạt tự động điều phối cho chi nhánh {}", branchId);
        int assignedCount = driverDispatchService.autoDispatchReadyOrders(branchId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "branchId", branchId,
                "assignedCount", assignedCount,
                "message", "Đã tự động phân công thành công " + assignedCount + " đơn hàng."
        ));
    }

    /**
     * Lấy dữ liệu snapshot hàng đợi chờ điều phối và trạng thái tài xế.
     */
    @GetMapping("/queue")
    public ResponseEntity<Map<String, Object>> getDispatchQueue(
            @RequestParam(value = "branchId", defaultValue = "1") Long branchId,
            HttpServletRequest request,
            Authentication authentication) {
        if (!isDispatchScopeAllowed(authentication, request, branchId, false)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        Map<String, Object> queue = driverDispatchService.getDispatchQueue(branchId);
        return ResponseEntity.ok(queue);
    }

    private boolean isDispatchScopeAllowed(Authentication authentication, HttpServletRequest request, Long id, boolean orderBased) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("POS_ACCOUNT") != null) {
            Object sessionBranch = session.getAttribute("POS_BRANCH_ID");
            if (sessionBranch == null) {
                return false;
            }
            if (orderBased) {
                return orderRepository.findById(id)
                        .map(o -> o.getBranch() != null && sessionBranch.equals(o.getBranch().getId()))
                        .orElse(false);
            }
            return id == null || sessionBranch.equals(id);
        }
        return true;
    }
}
