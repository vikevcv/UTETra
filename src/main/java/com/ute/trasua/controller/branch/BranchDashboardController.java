package com.ute.trasua.controller.branch;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller điều hướng trang chủ phân hệ Quản Lý Chi Nhánh (Branch Portal - Module 12).
 */
@Controller
@RequestMapping("/branch")
public class BranchDashboardController {

    /**
     * Tự động chuyển hướng từ gốc /branch hoặc /branch/dashboard sang khu vực quản lý nhân sự & ca trực (UC16).
     */
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(@RequestParam(value = "branchId", required = false) Long branchId) {
        if (branchId != null) {
            return "redirect:/branch/employees?branchId=" + branchId;
        }
        return "redirect:/branch/employees";
    }
}
