package com.ute.trasua.controller.branch;

import com.ute.trasua.dto.kitchen.BranchMenuStatusDto;
import com.ute.trasua.dto.kitchen.ToggleMenuRequest;
import com.ute.trasua.service.BranchMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API bật tắt trạng thái còn/hết hàng tại chi nhánh (UC11).
 */
@RestController
@RequestMapping("/api/branch-menu")
@RequiredArgsConstructor
@Slf4j
public class BranchMenuApiController {

    private final BranchMenuService branchMenuService;

    /**
     * Lấy danh sách món nước và topping kèm trạng thái còn/hết tại chi nhánh.
     */
    @GetMapping("/{branchId}")
    public ResponseEntity<List<BranchMenuStatusDto>> getBranchMenuStatus(@PathVariable Long branchId) {
        List<BranchMenuStatusDto> list = branchMenuService.getBranchMenuStatus(branchId);
        return ResponseEntity.ok(list);
    }

    /**
     * Bật/tắt trạng thái kinh doanh của món nước hoặc topping tại chi nhánh (UC11).
     */
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleAvailability(@RequestBody ToggleMenuRequest request) {
        boolean newStatus;
        if ("TOPPING".equalsIgnoreCase(request.getItemType())) {
            newStatus = branchMenuService.toggleToppingAvailability(
                    request.getBranchId(),
                    request.getItemId(),
                    request.getIsAvailable()
            );
        } else {
            newStatus = branchMenuService.toggleProductAvailability(
                    request.getBranchId(),
                    request.getItemId(),
                    request.getIsAvailable()
            );
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "itemId", request.getItemId(),
                "itemType", request.getItemType(),
                "isAvailable", newStatus,
                "message", newStatus ? "Đã chuyển sang CÒN HÀNG" : "Đã đánh dấu TẠM HẾT tại chi nhánh"
        ));
    }
}
