package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.user.ChangePasswordRequest;
import com.ute.trasua.dto.user.UpdateProfileRequest;
import com.ute.trasua.dto.user.UserProfileDto;
import com.ute.trasua.entity.User;
import com.ute.trasua.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý Hồ Sơ Cá Nhân dành riêng cho Quản Trị Viên Chuỗi (Administrator).
 */
@Controller
@RequestMapping("/admin/profile")
@RequiredArgsConstructor
@Slf4j
public class AdminProfileController {

    private final UserService userService;

    @GetMapping
    public String showAdminProfile(Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        UserProfileDto profile = userService.getUserProfile(currentUser.getUsername());
        model.addAttribute("profile", profile);
        model.addAttribute("pageTitle", "Hồ Sơ Quản Trị Viên Chuỗi — " + (profile.getFullName() != null ? profile.getFullName() : profile.getUsername()));
        return "admin/profile/index";
    }

    @PostMapping
    public String updateAdminProfile(@Valid @ModelAttribute("updateRequest") UpdateProfileRequest request,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/profile";
        }

        try {
            userService.updateProfile(currentUser.getUsername(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ Quản trị viên thành công!");
        } catch (Exception e) {
            log.error("Lỗi cập nhật hồ sơ Admin {}: {}", currentUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật hồ sơ: " + e.getMessage());
        }

        return "redirect:/admin/profile";
    }

    @PostMapping("/avatar")
    public String updateAdminAvatar(@RequestParam("avatarFile") MultipartFile avatarFile,
                                    RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        if (avatarFile == null || avatarFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn hình ảnh để tải lên làm ảnh đại diện.");
            return "redirect:/admin/profile";
        }

        try {
            userService.updateAvatar(currentUser.getUsername(), avatarFile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ảnh đại diện Quản trị viên thành công!");
        } catch (Exception e) {
            log.error("Lỗi cập nhật avatar Admin {}: {}", currentUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật ảnh đại diện: " + e.getMessage());
        }

        return "redirect:/admin/profile";
    }

    @PostMapping("/password")
    public String changeAdminPassword(@Valid @ModelAttribute("passwordRequest") ChangePasswordRequest request,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/profile";
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            return "redirect:/admin/profile";
        }

        try {
            userService.changePassword(currentUser.getUsername(), request.getCurrentPassword(), request.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu tài khoản Quản trị viên thành công!");
        } catch (Exception e) {
            log.warn("Đổi mật khẩu thất bại cho Admin {}: {}", currentUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/profile";
    }
}
