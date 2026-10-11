package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.UserResponse;
import com.ute.trasua.dto.UserSearchRequest;
import com.ute.trasua.dto.UserUpdateRequest;
import com.ute.trasua.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Set;

/**
 * Controller phục vụ Chủ chuỗi quản lý toàn bộ tài khoản người dùng và phân quyền (Module 11 - UC20).
 */
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class UserManagementController {

    private final UserManagementService userService;

    /**
     * Màn hình danh sách người dùng kèm bộ lọc tìm kiếm và phân trang (UC20).
     */
    @GetMapping
    public String listUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "locked", required = false) Boolean locked,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        UserSearchRequest searchRequest = UserSearchRequest.builder()
                .keyword(keyword)
                .role(role)
                .locked(locked)
                .page(page)
                .size(size)
                .build();

        Page<UserResponse> usersPage = userService.searchUsers(searchRequest);
        Map<String, Object> stats = userService.getUserStatistics();

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("stats", stats);
        model.addAttribute("searchRequest", searchRequest);
        model.addAttribute("pageTitle", "Quản Lý Người Dùng Toàn Chuỗi — UTETra");

        return "admin/users/list";
    }

    /**
     * Xem hồ sơ chi tiết của một người dùng (UC20).
     */
    @GetMapping("/{id}")
    public String viewUserDetail(@PathVariable("id") Long id, Model model) {
        try {
            UserResponse user = userService.getUserById(id);
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "Chi Tiết Người Dùng: " + user.getUsername());
            return "admin/users/detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /**
     * Xử lý khóa tài khoản người dùng kèm ghi chú lý do (UC20).
     */
    @PostMapping("/{id}/lock")
    public String lockUser(
            @PathVariable("id") Long id,
            @RequestParam(value = "reason", required = false, defaultValue = "Vi phạm quy định hệ thống") String reason,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.toggleLock(id, true, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã khóa tài khoản thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi khóa user {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Xử lý mở khóa tài khoản người dùng (UC20).
     */
    @PostMapping("/{id}/unlock")
    public String unlockUser(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.toggleLock(id, false, null);
            redirectAttributes.addFlashAttribute("successMessage", "Đã mở khóa tài khoản thành công! Người dùng có thể đăng nhập lại.");
        } catch (Exception e) {
            log.error("Lỗi khi mở khóa user {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Cập nhật thông tin hoặc thay đổi vai trò (role) của người dùng (UC20).
     */
    @PostMapping("/{id}/update")
    public String updateUser(
            @PathVariable("id") Long id,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "role", required = false) String role,
            RedirectAttributes redirectAttributes
    ) {
        try {
            UserUpdateRequest.UserUpdateRequestBuilder builder = UserUpdateRequest.builder()
                    .fullName(fullName)
                    .phone(phone);

            if (role != null && !role.isBlank()) {
                builder.roles(Set.of(role));
            }

            userService.updateUser(id, builder.build());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin tài khoản thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật user {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
