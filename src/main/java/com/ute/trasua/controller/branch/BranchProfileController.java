package com.ute.trasua.controller.branch;

import com.ute.trasua.dto.BranchResponse;
import com.ute.trasua.dto.user.ChangePasswordRequest;
import com.ute.trasua.dto.user.UpdateProfileRequest;
import com.ute.trasua.dto.user.UserProfileDto;
import com.ute.trasua.entity.User;
import com.ute.trasua.service.BranchService;
import com.ute.trasua.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý Hồ Sơ Cá Nhân dành riêng cho Quản Lý Chi Nhánh được phân công riêng cơ sở (Branch Manager).
 * ĐẢM BẢO: Quản trị viên (Admin) khi vào phân hệ chi nhánh KHÔNG quản lý profile ở đây (sẽ được điều hướng về /admin/profile).
 */
@Controller
@RequestMapping("/branch/profile")
@RequiredArgsConstructor
@Slf4j
public class BranchProfileController {

    private final UserService userService;
    private final BranchService branchService;

    @GetMapping
    public String showBranchManagerProfile(Authentication authentication,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        // 1. Kiểm tra: Nếu là Admin vào phân hệ chi nhánh -> KHÔNG được quản lý profile ở đây, điều hướng về /admin/profile
        if (isAdmin(authentication, currentUser)) {
            redirectAttributes.addFlashAttribute("warningMessage",
                    "Quản trị viên vui lòng quản lý hồ sơ tại phân hệ Quản trị hệ thống (/admin/profile).");
            return "redirect:/admin/profile";
        }

        // 2. Kiểm tra xem người dùng có phải là Quản lý được phân công riêng cho một chi nhánh hay không
        BranchResponse managedBranch = branchService.getBranchByManagerUsername(currentUser.getUsername()).orElse(null);
        if (managedBranch == null) {
            log.warn("Người dùng {} truy cập /branch/profile nhưng chưa được phân công chi nhánh", currentUser.getUsername());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Tài khoản của bạn chưa được phân công phụ trách chi nhánh cụ thể nào.");
            return "redirect:/branch/dashboard";
        }

        UserProfileDto profile = userService.getUserProfile(currentUser.getUsername());
        model.addAttribute("profile", profile);
        model.addAttribute("managedBranch", managedBranch);
        model.addAttribute("currentBranch", managedBranch);
        model.addAttribute("pageTitle", "Hồ Sơ Quản Lý Cơ Sở — " + managedBranch.getName());
        return "branch/profile/index";
    }

    @PostMapping
    public String updateBranchManagerProfile(Authentication authentication,
                                            @Valid @ModelAttribute("updateRequest") UpdateProfileRequest request,
                                            BindingResult bindingResult,
                                            RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        if (isAdmin(authentication, currentUser)) {
            redirectAttributes.addFlashAttribute("warningMessage",
                    "Quản trị viên vui lòng quản lý hồ sơ tại phân hệ Quản trị hệ thống (/admin/profile).");
            return "redirect:/admin/profile";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/branch/profile";
        }

        try {
            userService.updateProfile(currentUser.getUsername(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ Quản lý chi nhánh thành công!");
        } catch (Exception e) {
            log.error("Lỗi cập nhật hồ sơ Manager {}: {}", currentUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật hồ sơ: " + e.getMessage());
        }

        return "redirect:/branch/profile";
    }

    @PostMapping("/avatar")
    public String updateBranchManagerAvatar(Authentication authentication,
                                           @RequestParam("avatarFile") MultipartFile avatarFile,
                                           RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        if (isAdmin(authentication, currentUser)) {
            redirectAttributes.addFlashAttribute("warningMessage",
                    "Quản trị viên vui lòng quản lý hồ sơ tại phân hệ Quản trị hệ thống (/admin/profile).");
            return "redirect:/admin/profile";
        }

        if (avatarFile == null || avatarFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn hình ảnh để tải lên làm ảnh đại diện.");
            return "redirect:/branch/profile";
        }

        try {
            userService.updateAvatar(currentUser.getUsername(), avatarFile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ảnh đại diện Quản lý chi nhánh thành công!");
        } catch (Exception e) {
            log.error("Lỗi cập nhật avatar Manager {}: {}", currentUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật ảnh đại diện: " + e.getMessage());
        }

        return "redirect:/branch/profile";
    }

    @PostMapping("/password")
    public String changeBranchManagerPassword(Authentication authentication,
                                             @Valid @ModelAttribute("passwordRequest") ChangePasswordRequest request,
                                             BindingResult bindingResult,
                                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        if (isAdmin(authentication, currentUser)) {
            redirectAttributes.addFlashAttribute("warningMessage",
                    "Quản trị viên vui lòng quản lý hồ sơ tại phân hệ Quản trị hệ thống (/admin/profile).");
            return "redirect:/admin/profile";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/branch/profile";
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            return "redirect:/branch/profile";
        }

        try {
            userService.changePassword(currentUser.getUsername(), request.getCurrentPassword(), request.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu tài khoản Quản lý chi nhánh thành công!");
        } catch (Exception e) {
            log.warn("Đổi mật khẩu thất bại cho Manager {}: {}", currentUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/branch/profile";
    }

    private boolean isAdmin(Authentication authentication, User currentUser) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()) || "ADMIN".equalsIgnoreCase(a.getAuthority()))) {
            return true;
        }
        if (currentUser != null && currentUser.getRoles() != null) {
            return currentUser.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r) || "ADMIN".equalsIgnoreCase(r));
        }
        return false;
    }
}
