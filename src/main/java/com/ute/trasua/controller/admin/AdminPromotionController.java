package com.ute.trasua.controller.admin;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Controller for Chain Owner (Admin) to manage promotions chain-wide and per branch (Module 4 - UC24).
 * Conforms to business form QLKM_BM1.
 */
@Controller
@RequestMapping("/admin/promotions")
@Slf4j
public class AdminPromotionController {

    private final PromotionService promotionService;
    private final BranchService branchService;

    public AdminPromotionController(PromotionService promotionService, BranchService branchService) {
        this.promotionService = promotionService;
        this.branchService = branchService;
    }

    /**
     * Display centralized promotions management dashboard.
     */
    @GetMapping
    public String index(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        Page<PromotionResponse> promotionsPage = promotionService.searchPromotionsForAdmin(
                keyword, scope, branchId, status, PageRequest.of(page, size)
        );
        Map<String, Object> stats = promotionService.getPromotionStatistics(null);
        List<BranchResponse> branches = branchService.getAllBranches();

        model.addAttribute("promotionsPage", promotionsPage);
        model.addAttribute("stats", stats);
        model.addAttribute("branches", branches);
        model.addAttribute("keyword", keyword);
        model.addAttribute("scope", scope);
        model.addAttribute("branchId", branchId);
        model.addAttribute("status", status);
        model.addAttribute("newPromotion", new CreatePromotionRequest());
        model.addAttribute("pageTitle", "Quản Lý Khuyến Mãi Chuỗi");

        return "admin/promotions/index";
    }

    @PostMapping("/create")
    public String createPromotion(
            @Valid @ModelAttribute("newPromotion") CreatePromotionRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/promotions";
        }

        try {
            PromotionResponse created = promotionService.createPromotion(request, null, true);
            redirectAttributes.addFlashAttribute("successMessage", "Phát hành voucher '" + created.getVoucherCode() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/promotions";
    }

    @PostMapping(path = {"/{id}/update", "/{id}/edit"})
    public String updatePromotion(
            @PathVariable Long id,
            @Valid @ModelAttribute UpdatePromotionRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/promotions";
        }

        try {
            promotionService.updatePromotion(id, request, null, true);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chương trình khuyến mãi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/promotions";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean active = promotionService.togglePromotionStatus(id, null, true);
            redirectAttributes.addFlashAttribute("successMessage",
                    active ? "Đã kích hoạt lại mã khuyến mãi!" : "Đã tạm dừng mã khuyến mãi!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/promotions";
    }

    @PostMapping("/{id}/delete")
    public String deletePromotion(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            promotionService.deletePromotion(id, null, true);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa chương trình khuyến mãi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/promotions";
    }
}
