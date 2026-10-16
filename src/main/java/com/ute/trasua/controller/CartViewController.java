package com.ute.trasua.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller serving the customer-facing Cart page (Module 1 - UC04).
 */
@Controller
@RequestMapping("/cart")
public class CartViewController {

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("siteTitle", "UTETra — Khay Trà & Giỏ Hàng Của Bạn");
        return "cart/index";
    }
}
