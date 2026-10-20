package com.ute.trasua.controller;

import com.ute.trasua.dto.user.UserAddressDto;
import com.ute.trasua.dto.user.UserAddressRequest;
import com.ute.trasua.entity.User;
import com.ute.trasua.service.UserAddressService;
import com.ute.trasua.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller hiển thị và quản lý Sổ địa chỉ giao hàng (Delivery Address Book) của khách hàng.
 */
@Controller
@RequestMapping("/user/addresses")
@RequiredArgsConstructor
@Slf4j
public class UserAddressViewController {

    private final UserAddressService userAddressService;
    private final UserService userService;

    @GetMapping
    public String viewAddressesPage(@RequestParam(value = "returnUrl", required = false) String returnUrl,
                                    Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            String target = "/auth/login";
            if (returnUrl != null && !returnUrl.isBlank()) {
                target += "?redirect=" + returnUrl;
            }
            return "redirect:" + target;
        }

        List<UserAddressDto> addresses = userAddressService.getAddressesByUserId(currentUser.getId());
        UserAddressDto defaultAddress = userAddressService.getDefaultAddress(currentUser.getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("addresses", addresses);
        model.addAttribute("defaultAddress", defaultAddress);
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("siteTitle", "Sổ Địa Chỉ Giao Hàng — UTETra Zen Gallery");

        return "user/addresses";
    }

    @PostMapping("/create")
    public String handleCreateAddress(@Valid @ModelAttribute("addressRequest") UserAddressRequest request,
                                      BindingResult bindingResult,
                                      @RequestParam(value = "returnUrl", required = false) String returnUrl,
                                      RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            String redirectPath = "/user/addresses";
            if (returnUrl != null && !returnUrl.isBlank()) {
                redirectPath += "?returnUrl=" + returnUrl;
            }
            return "redirect:" + redirectPath;
        }

        try {
            UserAddressDto created = userAddressService.createAddress(currentUser.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm địa chỉ giao hàng mới thành công!");

            // Nếu người dùng chuyển từ Checkout sang thì đưa họ quay lại Checkout với địa chỉ vừa tạo
            if (returnUrl != null && returnUrl.contains("/checkout")) {
                return "redirect:" + returnUrl + (returnUrl.contains("?") ? "&" : "?") + "selectedAddressId=" + created.getId();
            }
        } catch (Exception e) {
            log.error("Lỗi khi thêm địa chỉ cho user {}: {}", currentUser.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm địa chỉ: " + e.getMessage());
        }

        String redirectPath = "/user/addresses";
        if (returnUrl != null && !returnUrl.isBlank()) {
            redirectPath += "?returnUrl=" + returnUrl;
        }
        return "redirect:" + redirectPath;
    }

    @PostMapping("/{id}/update")
    public String handleUpdateAddress(@PathVariable("id") Long id,
                                      @Valid @ModelAttribute("addressRequest") UserAddressRequest request,
                                      BindingResult bindingResult,
                                      @RequestParam(value = "returnUrl", required = false) String returnUrl,
                                      RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            String redirectPath = "/user/addresses";
            if (returnUrl != null && !returnUrl.isBlank()) {
                redirectPath += "?returnUrl=" + returnUrl;
            }
            return "redirect:" + redirectPath;
        }

        try {
            userAddressService.updateAddress(id, currentUser.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật địa chỉ thành công!");

            if (returnUrl != null && returnUrl.contains("/checkout")) {
                return "redirect:" + returnUrl + (returnUrl.contains("?") ? "&" : "?") + "selectedAddressId=" + id;
            }
        } catch (Exception e) {
            log.error("Lỗi khi sửa địa chỉ ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật: " + e.getMessage());
        }

        String redirectPath = "/user/addresses";
        if (returnUrl != null && !returnUrl.isBlank()) {
            redirectPath += "?returnUrl=" + returnUrl;
        }
        return "redirect:" + redirectPath;
    }

    @PostMapping("/{id}/default")
    public String handleSetDefault(@PathVariable("id") Long id,
                                   @RequestParam(value = "returnUrl", required = false) String returnUrl,
                                   RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        try {
            userAddressService.setDefaultAddress(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã đặt làm địa chỉ giao hàng mặc định!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        String redirectPath = "/user/addresses";
        if (returnUrl != null && !returnUrl.isBlank()) {
            redirectPath += "?returnUrl=" + returnUrl;
        }
        return "redirect:" + redirectPath;
    }

    @PostMapping("/{id}/delete")
    public String handleDeleteAddress(@PathVariable("id") Long id,
                                      @RequestParam(value = "returnUrl", required = false) String returnUrl,
                                      RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        try {
            userAddressService.deleteAddress(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa địa chỉ khỏi sổ địa chỉ.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa địa chỉ: " + e.getMessage());
        }

        String redirectPath = "/user/addresses";
        if (returnUrl != null && !returnUrl.isBlank()) {
            redirectPath += "?returnUrl=" + returnUrl;
        }
        return "redirect:" + redirectPath;
    }
}
