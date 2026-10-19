package com.ute.trasua.controller.branch;

import com.ute.trasua.dto.BranchResponse;
import com.ute.trasua.dto.CreatePromotionRequest;
import com.ute.trasua.dto.PromotionResponse;
import com.ute.trasua.dto.UpdatePromotionRequest;
import com.ute.trasua.service.BranchService;
import com.ute.trasua.service.PromotionService;
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
 * Controller for Branch Manager to create and manage branch-specific promotions (Module 4 - UC18).
 * Conforms to business form QLKM_BM1.
 */
@Controller
@RequestMapping("/branch/promotions")
@Slf4j
public class BranchPromotionController {

    private final PromotionService promotionService;
    private final BranchService branchService;

    public BranchPromotionController(PromotionService promotionService, BranchService branchService) {
        this.promotionService = promotionService;
        this.branchService = branchService;
    }

    @GetMapping
    public String index(
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
            return "branch/promotions/index";
        }

        Page<PromotionResponse> promotionsPage = promotionService.searchPromotionsForBranch(
                currentBranch.getId(), keyword, status, PageRequest.of(page, size)
        );
        Map<String, Object> stats = promotionService.getPromotionStatistics(currentBranch.getId());

        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("promotionsPage", promotionsPage);
        model.addAttribute("stats", stats);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("newPromotion", new CreatePromotionRequest());
        model.addAttribute("pageTitle", "Khuyến Mãi Chi Nhánh - " + currentBranch.getName());

        return "branch/promotions/index";
    }

    @PostMapping("/create")
    public String createPromotion(
            @RequestParam(value = "currentBranchId", required = false) Long currentBranchId,
            @Valid @ModelAttribute("newPromotion") CreatePromotionRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(currentBranchId, authentication);
        if (currentBranch == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không xác định được chi nhánh để tạo khuyến mãi.");
            return "redirect:/branch/promotions";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/branch/promotions?branchId=" + currentBranch.getId();
        }

        try {
            request.setScope("BRANCH");
            request.setBranchId(currentBranch.getId());
            boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            PromotionResponse created = promotionService.createPromotion(request, currentBranch.getId(), isAdmin);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tạo mã khuyến mãi '" + created.getVoucherCode() + "' cho chi nhánh thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/promotions?branchId=" + currentBranch.getId();
    }

    @PostMapping("/{id}/update")
    public String updatePromotion(
            @PathVariable Long id,
            @RequestParam(value = "branchId", required = false) Long branchId,
            @Valid @ModelAttribute UpdatePromotionRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        Long effectiveBranchId = currentBranch != null ? currentBranch.getId() : null;

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/branch/promotions" + (effectiveBranchId != null ? "?branchId=" + effectiveBranchId : "");
        }

        try {
            boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            promotionService.updatePromotion(id, request, effectiveBranchId, isAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật khuyến mãi chi nhánh thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/promotions" + (effectiveBranchId != null ? "?branchId=" + effectiveBranchId : "");
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(
            @PathVariable Long id,
            @RequestParam(value = "branchId", required = false) Long branchId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        Long effectiveBranchId = currentBranch != null ? currentBranch.getId() : null;

        try {
            boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            boolean active = promotionService.togglePromotionStatus(id, effectiveBranchId, isAdmin);
            redirectAttributes.addFlashAttribute("successMessage",
                    active ? "Đã mở lại mã khuyến mãi!" : "Đã tạm dừng mã khuyến mãi chi nhánh!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/promotions" + (effectiveBranchId != null ? "?branchId=" + effectiveBranchId : "");
    }

    @PostMapping("/{id}/delete")
    public String deletePromotion(
            @PathVariable Long id,
            @RequestParam(value = "branchId", required = false) Long branchId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        Long effectiveBranchId = currentBranch != null ? currentBranch.getId() : null;

        try {
            boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            promotionService.deletePromotion(id, effectiveBranchId, isAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa khuyến mãi chi nhánh thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/promotions" + (effectiveBranchId != null ? "?branchId=" + effectiveBranchId : "");
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
