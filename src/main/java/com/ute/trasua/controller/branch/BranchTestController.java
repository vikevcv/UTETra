package com.ute.trasua.controller.branch;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller kiểm thử Layout phân hệ Quản Lý Chi Nhánh (QLCN).
 */
@Controller
@RequestMapping("/branch")
public class BranchTestController {

    @GetMapping("/test")
    public String testBranchLayout(Model model) {
        model.addAttribute("currentBranchName", "Chi Nhánh 01 - Võ Văn Ngân, Thủ Đức");
        model.addAttribute("successMessage", "Giao diện Quản Lý Chi Nhánh (branch.jsp) hoạt động hoàn toàn tách biệt!");
        return "branch/test";
    }
}
