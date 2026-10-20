package com.ute.trasua.controller.branch;

import com.ute.trasua.dto.kitchen.BranchMenuStatusDto;
import com.ute.trasua.dto.kitchen.ToggleMenuRequest;
import com.ute.trasua.service.BranchMenuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    public ResponseEntity<List<BranchMenuStatusDto>> getBranchMenuStatus(
            @PathVariable Long branchId,
            HttpServletRequest request) {
        if (!isPosBranchAllowed(request, branchId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        List<BranchMenuStatusDto> list = branchMenuService.getBranchMenuStatus(branchId);
        return ResponseEntity.ok(list);
    }

    /**
     * Bật/tắt trạng thái kinh doanh của món nước hoặc topping tại chi nhánh (UC11).
     */
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleAvailability(
            @RequestBody ToggleMenuRequest body,
            HttpServletRequest request) {
        if (!isPosBranchAllowed(request, body.getBranchId())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        boolean newStatus;
        if ("TOPPING".equalsIgnoreCase(body.getItemType())) {
            newStatus = branchMenuService.toggleToppingAvailability(
                    body.getBranchId(),
                    body.getItemId(),
                    body.getIsAvailable()
            );
        } else {
            newStatus = branchMenuService.toggleProductAvailability(
                    body.getBranchId(),
                    body.getItemId(),
                    body.getIsAvailable()
            );
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "itemId", body.getItemId(),
                "itemType", body.getItemType(),
                "isAvailable", newStatus,
                "message", newStatus ? "Đã chuyển sang CÒN HÀNG" : "Đã đánh dấu TẠM HẾT tại chi nhánh"
        ));
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
}
