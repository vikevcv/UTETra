package com.ute.trasua.controller.branch;

import com.ute.trasua.dto.BranchResponse;
import com.ute.trasua.dto.complaint.ComplaintResolveRequest;
import com.ute.trasua.dto.complaint.OrderComplaintDto;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.BranchService;
import com.ute.trasua.service.OrderComplaintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Controller Quản Lý Khiếu Nại Đơn Hàng Tại Phân Hệ Chi Nhánh (Branch Portal).
 * Ràng buộc bảo mật: Quản lý chi nhánh chỉ được xem và xử lý các khiếu nại của chính cơ sở mình phụ trách.
 */
@Controller
@RequestMapping("/branch/complaints")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class BranchComplaintController {

    private final OrderComplaintService orderComplaintService;
    private final BranchService branchService;
    private final UserRepository userRepository;

    /**
     * Danh sách khiếu nại đơn hàng thuộc chi nhánh.
     */
    @GetMapping
    public String listBranchComplaints(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
            Authentication authentication,
            Model model) {

        boolean isAdmin = isUserAdmin(authentication);
        BranchResponse currentBranch = resolveBranch(branchId, authentication, isAdmin);

        if (currentBranch == null) {
            model.addAttribute("error", "Không xác định được chi nhánh tương ứng.");
            model.addAttribute("complaints", Collections.emptyList());
            model.addAttribute("currentStatus", status);
            model.addAttribute("siteTitle", "Khiếu Nại Đơn Hàng — UTETra Branch");
            return "branch/complaints/list";
        }

        List<OrderComplaintDto> complaints = orderComplaintService.getComplaintsByBranch(currentBranch.getId(), status);
        List<BranchResponse> allBranches = isAdmin ? branchService.getAllBranches() : List.of(currentBranch);

        model.addAttribute("complaints", complaints);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("branches", allBranches);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("siteTitle", "Khiếu Nại Đơn Hàng — " + currentBranch.getName());

        return "branch/complaints/list";
    }

    /**
     * Xử lý giải quyết khiếu nại tại chi nhánh (Bồi hoàn voucher hoặc từ chối).
     */
    @PostMapping("/{id}/resolve")
    @ResponseBody
    public ResponseEntity<?> resolveComplaint(
            @PathVariable("id") Long id,
            @RequestBody ComplaintResolveRequest request,
            Authentication authentication) {

        User currentUser = null;
        if (authentication != null && authentication.isAuthenticated()) {
            currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        }

        try {
            OrderComplaintDto result = orderComplaintService.resolveComplaint(id, request, currentUser);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã xử lý khiếu nại cho chi nhánh thành công!",
                    "complaint", result
            ));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Lỗi xử lý khiếu nại #{} tại chi nhánh: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Có lỗi hệ thống khi xử lý khiếu nại."
            ));
        }
    }

    private boolean isUserAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()) || "ADMIN".equalsIgnoreCase(a.getAuthority()));
    }

    private BranchResponse resolveBranch(Long requestedBranchId, Authentication authentication, boolean isAdmin) {
        // 1. Nếu không phải ADMIN (là Quản lý chi nhánh), khóa chặt vào chi nhánh mà user quản lý
        if (!isAdmin && authentication != null && authentication.isAuthenticated()) {
            var branchOpt = branchService.getBranchByManagerUsername(authentication.getName());
            if (branchOpt.isPresent()) {
                return branchOpt.get();
            }
        }

        // 2. Nếu là ADMIN, cho phép chọn branchId hoặc mặc định chi nhánh đầu tiên
        if (requestedBranchId != null) {
            try {
                return branchService.getBranchById(requestedBranchId);
            } catch (Exception ignored) {}
        }

        List<BranchResponse> all = branchService.getAllBranches();
        return all.isEmpty() ? null : all.get(0);
    }
}
