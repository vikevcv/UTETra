package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.SizeDto;
import com.ute.trasua.dto.SizeRequest;
import com.ute.trasua.dto.ToppingDto;
import com.ute.trasua.dto.ToppingRequest;
import com.ute.trasua.service.ProductOptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Controller phục vụ Chủ Chuỗi (Admin) quản lý Kích cỡ (Size) và Topping toàn chuỗi (Module 5 - UC23).
 * Ánh xạ chuẩn theo 2 bảng KichCo và Topping trong sơ đồ ERD Báo Cáo SE.
 */
@Controller
@RequestMapping("/admin/product-options")
@RequiredArgsConstructor
@Slf4j
public class AdminProductOptionController {

    private final ProductOptionService optionService;

    /**
     * Màn hình danh sách tùy chọn phân loại theo 2 Tab: Kích cỡ ly & Topping.
     */
    @GetMapping
    public String index(
            @RequestParam(value = "activeTab", defaultValue = "size") String activeTab,
            Model model
    ) {
        List<SizeDto> sizes = optionService.getAllSizes();
        List<ToppingDto> toppings = optionService.getAllToppings();
        Map<String, Object> stats = optionService.getOptionStatistics();

        model.addAttribute("sizes", sizes);
        model.addAttribute("toppings", toppings);
        model.addAttribute("stats", stats);
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("newSize", new SizeRequest());
        model.addAttribute("newTopping", new ToppingRequest());
        model.addAttribute("pageTitle", "Quản Lý Kích Cỡ & Topping");

        return "admin/product-options/index";
    }

    // ==================== KÍCH CỠ (SIZE) ====================

    @PostMapping("/size/create")
    public String createSize(
            @Valid @ModelAttribute("newSize") SizeRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/product-options?activeTab=size";
        }

        try {
            optionService.createSize(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm kích cỡ '" + request.getTenKichCo() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/product-options?activeTab=size";
    }

    @PostMapping("/size/update/{code}")
    public String updateSize(
            @PathVariable String code,
            @Valid @ModelAttribute SizeRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/product-options?activeTab=size";
        }

        try {
            optionService.updateSize(code, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật kích cỡ '" + request.getTenKichCo() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/product-options?activeTab=size";
    }

    @PostMapping("/size/toggle/{code}")
    public String toggleSize(
            @PathVariable String code,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean active = optionService.toggleSizeStatus(code);
            redirectAttributes.addFlashAttribute("successMessage",
                    active ? "Đã mở bán kích cỡ '" + code + "'!" : "Đã tạm dừng kích cỡ '" + code + "'!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/product-options?activeTab=size";
    }

    @PostMapping("/size/delete/{code}")
    public String deleteSize(
            @PathVariable String code,
            RedirectAttributes redirectAttributes
    ) {
        try {
            optionService.deleteSize(code);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa kích cỡ '" + code + "'!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/product-options?activeTab=size";
    }

    // ==================== TOPPING ====================

    @PostMapping("/topping/create")
    public String createTopping(
            @Valid @ModelAttribute("newTopping") ToppingRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/product-options?activeTab=topping";
        }

        try {
            optionService.createTopping(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm topping '" + request.getTenTopping() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/product-options?activeTab=topping";
    }

    @PostMapping("/topping/update/{id}")
    public String updateTopping(
            @PathVariable Long id,
            @Valid @ModelAttribute ToppingRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/product-options?activeTab=topping";
        }

        try {
            optionService.updateTopping(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật topping '" + request.getTenTopping() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/product-options?activeTab=topping";
    }

    @PostMapping("/topping/toggle/{id}")
    public String toggleTopping(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean active = optionService.toggleToppingStatus(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    active ? "Đã mở bán topping!" : "Đã tạm dừng topping!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/product-options?activeTab=topping";
    }

    @PostMapping("/topping/delete/{id}")
    public String deleteTopping(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            optionService.deleteTopping(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa topping thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/product-options?activeTab=topping";
    }
}
