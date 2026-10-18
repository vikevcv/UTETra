package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.complaint.ComplaintResolveRequest;
import com.ute.trasua.dto.complaint.OrderComplaintDto;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.OrderComplaintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/complaints")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminComplaintController {

    private final OrderComplaintService orderComplaintService;
    private final UserRepository userRepository;

    @GetMapping
    public String listComplaints(
            @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
            Model model) {

        List<OrderComplaintDto> complaints = orderComplaintService.getAllComplaints(status);
        model.addAttribute("complaints", complaints);
        model.addAttribute("currentStatus", status);
        model.addAttribute("siteTitle", "Quản Lý Khiếu Nại Đơn Hàng — UTETra Admin");
        return "admin/complaints/list";
    }

    @PostMapping("/{id}/resolve")
    @ResponseBody
    public ResponseEntity<?> resolveComplaint(
            @PathVariable("id") Long id,
            @RequestBody ComplaintResolveRequest request,
            Authentication authentication) {

        User adminUser = null;
        if (authentication != null && authentication.isAuthenticated()) {
            adminUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        }

        try {
            OrderComplaintDto result = orderComplaintService.resolveComplaint(id, request, adminUser);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã xử lý khiếu nại thành công!",
                    "complaint", result
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Lỗi xử lý khiếu nại #{}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Có lỗi hệ thống khi xử lý khiếu nại."
            ));
        }
    }
}
