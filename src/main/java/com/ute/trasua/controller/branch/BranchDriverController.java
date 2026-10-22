package com.ute.trasua.controller.branch;

import com.ute.trasua.dto.BranchResponse;
import com.ute.trasua.dto.CreateDriverRequest;
import com.ute.trasua.dto.DriverResponse;
import com.ute.trasua.dto.UpdateDriverRequest;
import com.ute.trasua.service.BranchService;
import com.ute.trasua.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Controller phục vụ Quản lý chi nhánh quản lý và cấp tài khoản đội ngũ tài xế giao hàng (Module 5 - UC17).
 */
@Controller
@RequestMapping("/branch/drivers")
@Slf4j
public class BranchDriverController {

    private final DriverService driverService;
    private final BranchService branchService;

    public BranchDriverController(DriverService driverService, BranchService branchService) {
        this.driverService = driverService;
        this.branchService = branchService;
    }

    /**
     * Màn hình danh sách tài xế thuộc chi nhánh phụ trách.
     */
    @GetMapping
    public String listBranchDrivers(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication authentication,
            Model model
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        if (currentBranch == null) {
            model.addAttribute("errorMessage", "Không tìm thấy thông tin chi nhánh liên kết.");
            return "branch/drivers/list";
        }

        Page<DriverResponse> driversPage = driverService.searchDriversByBranch(
                currentBranch.getId(),
                keyword,
                status,
                PageRequest.of(Math.max(0, page), size > 0 ? size : 10)
        );

        Map<String, Object> stats = driverService.getBranchDriverStatistics(currentBranch.getId());
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()) || "ADMIN".equalsIgnoreCase(a.getAuthority()));
        List<BranchResponse> allBranches = isAdmin ? branchService.getAllBranches()
                : (currentBranch != null ? List.of(currentBranch) : List.of());

        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("allBranches", allBranches);
        model.addAttribute("driversPage", driversPage);
        model.addAttribute("stats", stats);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("pageTitle", "Đội Ngũ Tài Xế — " + currentBranch.getName());
        model.addAttribute("newDriver", new CreateDriverRequest());

        return "branch/drivers/list";
    }

    /**
     * Cấp tài khoản tài xế mới và tự động gửi email thông tin đăng nhập.
     */
    @PostMapping("/create")
    public String createDriver(
            @Valid @ModelAttribute("newDriver") CreateDriverRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "currentBranchId", required = false) Long currentBranchId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(currentBranchId, authentication);
        Long effectiveBranchId = currentBranch != null ? currentBranch.getId() : currentBranchId;

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/branch/drivers" + (effectiveBranchId != null ? "?branchId=" + effectiveBranchId : "");
        }

        try {
            boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()) || "ADMIN".equalsIgnoreCase(a.getAuthority()));
            Long targetBranchId = (isAdmin && request.getBranchId() != null) ? request.getBranchId() : effectiveBranchId;
            DriverResponse created = driverService.createDriver(request, targetBranchId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cấp tài khoản tài xế '" + created.getFullName() + "' (" + created.getUsername() + ") thành công! Thông tin đăng nhập đã được gửi tới email " + created.getEmail());
        } catch (Exception e) {
            log.error("Lỗi cấp tài khoản tài xế: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/branch/drivers" + (effectiveBranchId != null ? "?branchId=" + effectiveBranchId : "");
    }

    /**
     * Cập nhật thông tin và lương tài xế (Module 5 - UC17).
     */
    @PostMapping("/{id}/update")
    public String updateDriver(
            @PathVariable Long id,
            @Valid @ModelAttribute UpdateDriverRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "branchId", required = false) Long branchId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        Long effectiveBranchId = currentBranch != null ? currentBranch.getId() : branchId;

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/branch/drivers" + (effectiveBranchId != null ? "?branchId=" + effectiveBranchId : "");
        }

        try {
            DriverResponse updated = driverService.updateDriver(id, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật thông tin và lương tài xế '" + updated.getFullName() + "' thành công!");
        } catch (Exception e) {
            log.error("Lỗi cập nhật tài xế [ID: {}]: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/branch/drivers" + (effectiveBranchId != null ? "?branchId=" + effectiveBranchId : "");
    }

    /**
     * Tạm dừng hoặc kích hoạt lại quyền nhận đơn của tài xế.
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleDriverStatus(
            @PathVariable Long id,
            @RequestParam boolean locked,
            @RequestParam(required = false, defaultValue = "Tạm ngưng tiếp nhận đơn") String reason,
            @RequestParam(value = "branchId", required = false) Long branchId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        Long effectiveBranchId = currentBranch != null ? currentBranch.getId() : branchId;

        try {
            driverService.toggleDriverLock(id, locked, reason);
            redirectAttributes.addFlashAttribute("successMessage",
                    locked ? "Đã tạm dừng quyền nhận đơn của tài xế!" : "Đã kích hoạt lại trạng thái nhận đơn cho tài xế!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/drivers" + (effectiveBranchId != null ? "?branchId=" + effectiveBranchId : "");
    }

    /**
     * Gửi lại mật khẩu mới qua email cho tài xế khi cần.
     */
    @PostMapping("/{id}/resend-email")
    public String resendCredentials(
            @PathVariable Long id,
            @RequestParam(value = "branchId", required = false) Long branchId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        Long effectiveBranchId = currentBranch != null ? currentBranch.getId() : branchId;

        try {
            String newPassword = driverService.resendDriverCredentials(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cấp lại mật khẩu thành công! Mật khẩu tạm mới: " + newPassword + " (Đã gửi email thông báo tới tài xế)");
        } catch (Exception e) {
            log.error("Lỗi cấp lại mật khẩu tài xế [ID: {}]: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/drivers" + (effectiveBranchId != null ? "?branchId=" + effectiveBranchId : "");
    }

    private BranchResponse resolveCurrentBranch(Long requestedBranchId, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()) || "ADMIN".equalsIgnoreCase(a.getAuthority()));
            if (!isAdmin) {
                String username = authentication.getName();
                return branchService.getBranchByManagerUsername(username).orElse(null);
            }
        }

        if (requestedBranchId != null) {
            try {
                return branchService.getBranchById(requestedBranchId);
            } catch (Exception ignored) {}
        }

        List<BranchResponse> all = branchService.getAllBranches();
        return all.isEmpty() ? null : all.get(0);
    }
}
