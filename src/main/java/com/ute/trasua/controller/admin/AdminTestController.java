package com.ute.trasua.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller kiểm thử SiteMesh Decorator Admin và Design System.
 * Phục vụ nghiệm thu Task 1.1 trong lộ trình phát triển Module 11-15.
 */
@Controller
@RequestMapping("/admin")
public class AdminTestController {

    @GetMapping("/test")
    public String testAdminLayout(Model model) {
        model.addAttribute("pageTitle", "Kiểm Thử Layout Quản Trị — UTETra Portal");
        model.addAttribute("successMessage", "SiteMesh Decorator Admin đã áp dụng thành công!");
        return "admin/test";
    }
}
