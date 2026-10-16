package com.ute.trasua.controller;

import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.OnlineOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller for rendering Online Checkout & Success views (Module 3 - UC05).
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class CheckoutViewController {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final OnlineOrderService onlineOrderService;

    @GetMapping("/checkout")
    public String checkoutPage(Model model) {
        List<Branch> activeBranches = branchRepository.findByActiveTrue();
        model.addAttribute("branches", activeBranches);

        User currentUser = resolveCurrentUser();
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("defaultCustomerName", currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getUsername());
            model.addAttribute("defaultCustomerPhone", currentUser.getPhone() != null ? currentUser.getPhone() : "");
            model.addAttribute("defaultCustomerEmail", currentUser.getEmail() != null ? currentUser.getEmail() : "");
        }

        model.addAttribute("siteTitle", "Thanh Toán & Đặt Hàng Trực Tuyến — UTETra Zen Gallery");
        return "checkout/index";
    }

    @GetMapping("/checkout/success")
    public String checkoutSuccessPage(@RequestParam("orderCode") String orderCode, Model model) {
        try {
            Order order = onlineOrderService.getOrderByCode(orderCode);
            model.addAttribute("order", order);
            model.addAttribute("siteTitle", "Đặt Hàng Thành Công #" + orderCode + " — UTETra");
            return "checkout/success";
        } catch (Exception e) {
            log.error("Không tìm thấy đơn hàng #{}: {}", orderCode, e.getMessage());
            return "redirect:/cart?error=order_not_found";
        }
    }

    private User resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
