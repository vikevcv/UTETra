package com.ute.trasua.controller;

import com.ute.trasua.service.BranchService;
import com.ute.trasua.service.CategoryService;
import com.ute.trasua.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * Controller chính xử lý điều hướng trang chủ UTETra.
 * Trả về view "home" được SiteMesh 3 Decorator bọc trong web.jsp.
 */
@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BranchService branchService;

    public HomeController(ProductService productService, CategoryService categoryService, BranchService branchService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.branchService = branchService;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "sort", required = false, defaultValue = "default") String sort,
            @RequestParam(value = "branchId", required = false) Long branchId,
            Model model) {

        var branches = branchService.getAllActiveBranches();
        Long effectiveBranchId = branchId;
        if (effectiveBranchId == null && branches != null && !branches.isEmpty()) {
            effectiveBranchId = branches.get(0).getId();
        }

        model.addAttribute("siteTitle", "UTETra — Modern Zen Wabi-Sabi & Haute Couture Tea Gallery");
        model.addAttribute("products", productService.filterProducts(keyword, categoryId, minPrice, maxPrice, sort, effectiveBranchId));
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("branches", branches);
        model.addAttribute("selectedBranchId", effectiveBranchId);

        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedSort", sort);
        return "home";
    }

    /**
     * Endpoint trung chuyển thông minh: tự động đưa người dùng về Portal tương ứng với quyền hiện tại.
     */
    @GetMapping("/portal")
    public String portal(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        var authorities = authentication.getAuthorities();
        if (authorities != null) {
            boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                return "redirect:/admin/dashboard";
            }
            boolean isManager = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
            if (isManager) {
                return "redirect:/branch/employees";
            }
            boolean isDriver = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_DRIVER"));
            if (isDriver) {
                return "redirect:/shipper";
            }
        }
        return "redirect:/";
    }
}
