package com.ute.trasua.controller;

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
 * Controller hiển thị và xử lý cập nhật Hồ sơ cá nhân người dùng.
 */
@Controller
@RequestMapping("/user/profile")
@RequiredArgsConstructor
@Slf4j
public class UserProfileViewController {

    private final UserService userService;

    @GetMapping
    public String viewProfilePage(Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        UserProfileDto profile = userService.getUserProfile(currentUser.getUsername());
        model.addAttribute("profile", profile);
        model.addAttribute("siteTitle", "Hồ Sơ Cá Nhân — " + (profile.getFullName() != null ? profile.getFullName() : profile.getUsername()) + " | UTETra");
        return "user/profile";
    }

    @PostMapping
    public String handleUpdateProfile(@Valid @ModelAttribute("updateRequest") UpdateProfileRequest request,
                                      BindingResult bindingResult,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            UserProfileDto profile = userService.getUserProfile(currentUser.getUsername());
            model.addAttribute("profile", profile);
            model.addAttribute("siteTitle", "Hồ Sơ Cá Nhân — UTETra");
            model.addAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "user/profile";
        }

        try {
            userService.updateProfile(currentUser.getUsername(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
        } catch (Exception e) {
            log.error("Lỗi cập nhật hồ sơ người dùng {}: {}", currentUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật hồ sơ: " + e.getMessage());
        }

        return "redirect:/user/profile";
    }

    @PostMapping("/avatar")
    public String handleUpdateAvatar(@RequestParam("avatarFile") MultipartFile avatarFile,
                                     RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        if (avatarFile == null || avatarFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn hình ảnh để tải lên làm ảnh đại diện.");
            return "redirect:/user/profile";
        }

        try {
            userService.updateAvatar(currentUser.getUsername(), avatarFile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ảnh đại diện mới thành công!");
        } catch (Exception e) {
            log.error("Lỗi cập nhật avatar cho {}: {}", currentUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật ảnh đại diện: " + e.getMessage());
        }

        return "redirect:/user/profile";
    }
}
