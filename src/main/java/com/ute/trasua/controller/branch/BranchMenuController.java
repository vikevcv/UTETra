package com.ute.trasua.controller.branch;

import com.ute.trasua.dto.BranchMenuResponse;
import com.ute.trasua.dto.BranchResponse;
import com.ute.trasua.dto.CategoryResponse;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.service.BranchMenuService;
import com.ute.trasua.service.BranchService;
import com.ute.trasua.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller quản lý thực đơn chi nhánh (Branch Menu Management)
 * Cho phép Quản lý kiểm soát menu đang bán, bật/tắt còn/hết hàng và đồng bộ món mới.
 */
@Controller
@Slf4j
public class BranchMenuController {

    private final BranchMenuService branchMenuService;
    private final BranchService branchService;
    private final CategoryService categoryService;
    private final BranchRepository branchRepository;

    public BranchMenuController(
            BranchMenuService branchMenuService,
            BranchService branchService,
            CategoryService categoryService,
            BranchRepository branchRepository
    ) {
        this.branchMenuService = branchMenuService;
        this.branchService = branchService;
        this.categoryService = categoryService;
        this.branchRepository = branchRepository;
    }

    /**
     * Giao diện chính Quản Lý Thực Đơn Chi Nhánh (Menu & Bật/Tắt còn hàng).
     */
    @GetMapping("/branch/menu")
    public String showBranchMenu(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "available", required = false) Boolean available,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Authentication authentication,
            Model model
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        Long targetBranchId = (currentBranch != null) ? currentBranch.getId() : null;

        List<BranchResponse> allBranches = branchService.getAllBranches();
        List<CategoryResponse> categories = categoryService.getAllCategories();

        Page<BranchMenuResponse> menuPage = Page.empty();
        long totalItems = 0;
        long availableCount = 0;
        long outOfStockCount = 0;
        int availabilityRate = 0;

        if (targetBranchId != null) {
            // Tự động đồng bộ thực đơn nếu chi nhánh chưa có dữ liệu menu
            if (branchMenuService.countTotalAtBranch(targetBranchId) == 0) {
                log.info(">>> Branch {} has empty menu. Auto-syncing from master product catalog...", targetBranchId);
                branchMenuService.syncAllProductsToBranch(targetBranchId);
            }

            Pageable pageable = PageRequest.of(Math.max(0, page), size);
            String searchKw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
            menuPage = branchMenuService.searchBranchMenu(targetBranchId, searchKw, categoryId, available, pageable);

            totalItems = branchMenuService.countTotalAtBranch(targetBranchId);
            availableCount = branchMenuService.countAvailableAtBranch(targetBranchId);
            outOfStockCount = Math.max(0, totalItems - availableCount);
            availabilityRate = totalItems > 0 ? (int) Math.round((double) availableCount / totalItems * 100) : 0;
        }

        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("branchId", targetBranchId);
        model.addAttribute("allBranches", allBranches);
        model.addAttribute("categories", categories);
        model.addAttribute("menuPage", menuPage);

        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("available", available);

        model.addAttribute("totalItems", totalItems);
        model.addAttribute("availableCount", availableCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("availabilityRate", availabilityRate);

        model.addAttribute("pageTitle", "Quản Lý Thực Đơn Chi Nhánh" +
                (currentBranch != null ? " — " + currentBranch.getName() : ""));

        return "branch/menu/list";
    }

    /**
     * AJAX endpoint bật/tắt trạng thái Còn hàng / Hết hàng cho 1 món tại chi nhánh.
     */
    @PostMapping("/branch/menu/toggle-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleStatus(
            @RequestParam("branchId") Long branchId,
            @RequestParam("productId") Long productId,
            @RequestParam("available") boolean available
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean updatedStatus = branchMenuService.updateProductAvailability(branchId, productId, available);
            long total = branchMenuService.countTotalAtBranch(branchId);
            long availableCount = branchMenuService.countAvailableAtBranch(branchId);
            long outOfStock = Math.max(0, total - availableCount);
            int rate = total > 0 ? (int) Math.round((double) availableCount / total * 100) : 0;

            response.put("success", true);
            response.put("available", updatedStatus);
            response.put("productId", productId);
            response.put("totalItems", total);
            response.put("availableCount", availableCount);
            response.put("outOfStockCount", outOfStock);
            response.put("availabilityRate", rate);
            response.put("message", updatedStatus ?
                    "Đã bật [CÒN HÀNG] cho món này tại chi nhánh!" :
                    "Đã chuyển sang [HẾT HÀNG / TẠM NGƯNG] tại chi nhánh!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error(">>> Error toggling product {} status at branch {}: {}", productId, branchId, e.getMessage());
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Endpoint thủ công cho phép Quản lý đồng bộ toàn bộ món từ thực đơn tổng vào chi nhánh.
     */
    @PostMapping("/branch/menu/sync")
    public String syncBranchMenu(
            @RequestParam("branchId") Long branchId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            branchMenuService.syncAllProductsToBranch(branchId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã đồng bộ đầy đủ toàn bộ món nước từ thực đơn chuỗi vào chi nhánh thành công!");
        } catch (Exception e) {
            log.error(">>> Error syncing menu for branch {}: {}", branchId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể đồng bộ: " + e.getMessage());
        }
        return "redirect:/branch/menu?branchId=" + branchId;
    }

    /**
     * Giao diện POS / KDS Barista quản lý tình trạng món (giữ tương thích ngược với Module 7 KDS).
     */
    @GetMapping({"/branch/menu-status", "/pos/menu-status"})
    public String branchMenuStatusView(@RequestParam(value = "branchId", required = false) Long branchId,
                                       HttpSession session,
                                       Model model) {
        List<Branch> branches = branchRepository.findAll();
        Branch currentBranch = null;

        if (branchId != null) {
            currentBranch = branchRepository.findById(branchId).orElse(null);
        } else {
            Long sessionBranchId = (Long) session.getAttribute("POS_BRANCH_ID");
            if (sessionBranchId != null) {
                currentBranch = branchRepository.findById(sessionBranchId).orElse(null);
            }
        }

        if (currentBranch == null && !branches.isEmpty()) {
            currentBranch = branches.get(0);
        }

        Long activeBranchId = (currentBranch != null) ? currentBranch.getId() : 1L;
        session.setAttribute("POS_BRANCH_ID", activeBranchId);

        String activeCashiers = (String) session.getAttribute("POS_ACTIVE_CASHIERS_NAME");
        if (activeCashiers == null || activeCashiers.isBlank()) {
            activeCashiers = "Quản lý / Barista";
        }

        model.addAttribute("branchId", activeBranchId);
        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("branchName", currentBranch != null ? currentBranch.getName() : "UTETra Flagship Thủ Đức");
        model.addAttribute("branches", branches);
        model.addAttribute("cashierUsername", activeCashiers);

        return "pos/menu_status";
    }

    /**
     * Helper xác định chi nhánh đang được quản lý.
     */
    private BranchResponse resolveCurrentBranch(Long requestedBranchId, Authentication authentication) {
        if (requestedBranchId != null) {
            try {
                return branchService.getBranchById(requestedBranchId);
            } catch (Exception ignored) {}
        }

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            var branchOpt = branchService.getBranchByManagerUsername(username);
            if (branchOpt.isPresent()) {
                return branchOpt.get();
            }
        }

        List<BranchResponse> all = branchService.getAllBranches();
        return all.isEmpty() ? null : all.get(0);
    }
}
