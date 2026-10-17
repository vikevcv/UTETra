package com.ute.trasua.controller.branch;

import com.ute.trasua.dto.dispatch.DispatchResultDto;
import com.ute.trasua.dto.dispatch.DriverAssignRequest;
import com.ute.trasua.service.DriverDispatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    /**
     * Phân công bắt buộc một đơn hàng cho tài xế trực ca.
     */
    @PostMapping("/orders/{orderId}/assign")
    public ResponseEntity<DispatchResultDto> assignOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody DriverAssignRequest request) {
        log.info("Quản lý yêu cầu phân công đơn {} cho tài xế {}", orderId, request.getDriverId());
        DispatchResultDto result = driverDispatchService.dispatchMandatoryOrder(orderId, request.getDriverId(), request.getNote());
        return ResponseEntity.ok(result);
    }

    /**
     * Điều phối lại một đơn hàng vừa gặp sự cố cho tài xế khác.
     */
    @PostMapping("/orders/{orderId}/reassign")
    public ResponseEntity<DispatchResultDto> reassignOrder(@PathVariable Long orderId) {
        log.info("Quản lý yêu cầu điều phối lại đơn {}", orderId);
        DispatchResultDto result = driverDispatchService.reassignCancelledOrder(orderId);
        return ResponseEntity.ok(result);
    }

    /**
     * Kích hoạt tự động quét hàng đợi chi nhánh và phân công theo 3 tầng ưu tiên.
     */
    @PostMapping("/orders/auto-dispatch")
    public ResponseEntity<Map<String, Object>> autoDispatch(
            @RequestParam(value = "branchId", defaultValue = "1") Long branchId) {
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
            @RequestParam(value = "branchId", defaultValue = "1") Long branchId) {
        Map<String, Object> queue = driverDispatchService.getDispatchQueue(branchId);
        return ResponseEntity.ok(queue);
    }
}
