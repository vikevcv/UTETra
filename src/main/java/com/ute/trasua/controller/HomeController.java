package com.ute.trasua.controller;

import com.ute.trasua.service.BranchService;
import com.ute.trasua.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller chính xử lý điều hướng trang chủ UTETra.
 * Trả về view "home" được SiteMesh 3 Decorator bọc trong web.jsp.
 */
@Controller
public class HomeController {

    private final ProductService productService;
    private final BranchService branchService;

    public HomeController(ProductService productService, BranchService branchService) {
        this.productService = productService;
        this.branchService = branchService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("siteTitle", "UTETra — Modern Zen Wabi-Sabi & Haute Couture Tea Gallery");
        model.addAttribute("products", productService.getActiveProducts());
        model.addAttribute("branches", branchService.getAllActiveBranches());
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
