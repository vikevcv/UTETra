package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.ProductOptionDto;
import com.ute.trasua.dto.ProductOptionRequest;
import com.ute.trasua.entity.ProductOptionType;
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
 * Controller phục vụ Chủ Chuỗi (Admin) quản lý tùy chọn Kích cỡ (Size) và Topping toàn chuỗi (Module 5 - UC23).
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
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model
    ) {
        List<ProductOptionDto> sizes = optionService.getAllOptions(ProductOptionType.SIZE, null, keyword);
        List<ProductOptionDto> toppings = optionService.getAllOptions(ProductOptionType.TOPPING, null, keyword);
        Map<String, Object> stats = optionService.getOptionStatistics();

        model.addAttribute("sizes", sizes);
        model.addAttribute("toppings", toppings);
        model.addAttribute("stats", stats);
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("keyword", keyword);
        model.addAttribute("newOption", new ProductOptionRequest());
        model.addAttribute("pageTitle", "Quản Lý Tùy Chọn Size & Topping — UTETra Executive Portal");

        return "admin/product-options/index";
    }

    /**
     * Thêm mới tùy chọn (Size hoặc Topping).
     */
    @PostMapping("/create")
    public String createOption(
            @Valid @ModelAttribute("newOption") ProductOptionRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/product-options?activeTab=" + 
                    (request.getType() == ProductOptionType.TOPPING ? "topping" : "size");
        }

        try {
            optionService.createOption(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Thêm tùy chọn '" + request.getName() + "' thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/product-options?activeTab=" + 
                (request.getType() == ProductOptionType.TOPPING ? "topping" : "size");
    }

    /**
     * Cập nhật thông tin và điều chỉnh giá phụ thu tùy chọn.
     */
    @PostMapping("/update/{id}")
    public String updateOption(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductOptionRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/product-options";
        }

        try {
            optionService.updateOption(id, request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Cập nhật tùy chọn '" + request.getName() + "' thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/product-options?activeTab=" + 
                (request.getType() == ProductOptionType.TOPPING ? "topping" : "size");
    }

    /**
     * Xóa tùy chọn khỏi hệ thống.
     */
    @PostMapping("/delete/{id}")
    public String deleteOption(
            @PathVariable Long id,
            @RequestParam(value = "activeTab", defaultValue = "size") String activeTab,
            RedirectAttributes redirectAttributes
    ) {
        try {
            optionService.deleteOption(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tùy chọn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/product-options?activeTab=" + activeTab;
    }
}
