package com.ute.trasua.controller;

import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.User;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.User;
import com.ute.trasua.service.BranchService;
import com.ute.trasua.service.OnlineOrderService;
import com.ute.trasua.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller for rendering Online Checkout & Success views (Module 3 - UC05).
 */
@Controller
@Slf4j
public class CheckoutViewController {

    private final BranchService branchService;
    private final UserService userService;
    private final OnlineOrderService onlineOrderService;

    public CheckoutViewController(BranchService branchService,
                                  UserService userService,
                                  OnlineOrderService onlineOrderService) {
        this.branchService = branchService;
        this.userService = userService;
        this.onlineOrderService = onlineOrderService;
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model) {
        List<Branch> activeBranches = branchService.getActiveBranchEntities();
        model.addAttribute("branches", activeBranches);

        User currentUser = userService.getCurrentAuthenticatedUser();
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
}
