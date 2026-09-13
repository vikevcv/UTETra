package com.ute.trasua.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller chính xử lý điều hướng trang chủ UTETra.
 * Trả về view "home" được SiteMesh 3 Decorator bọc trong web.jsp.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("siteTitle", "UTETra — Modern Zen Wabi-Sabi & Haute Couture Tea Gallery");
        return "home";
    }
}
