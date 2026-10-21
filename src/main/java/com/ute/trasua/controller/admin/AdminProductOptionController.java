package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.ProductSizeDto;
import com.ute.trasua.dto.ProductSizeRequest;
import com.ute.trasua.dto.ToppingDto;
import com.ute.trasua.dto.ToppingRequest;
import com.ute.trasua.service.ProductOptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Controller for Admin to manage cup sizes and toppings chain-wide (Module 5 - UC23).
 * Maps to tables 'kich_co' and 'topping' in the SE project ERD.
 */
@Controller
@RequestMapping("/admin/product-options")
@RequiredArgsConstructor
@Slf4j
public class AdminProductOptionController {

    private final ProductOptionService optionService;

    /**
     * Display option list with 2 tabs: Cup Sizes & Toppings.
     */
    @GetMapping
    public String index(
            @RequestParam(value = "activeTab", defaultValue = "size") String activeTab,
            Model model
    ) {
        List<ProductSizeDto> sizes = optionService.getAllSizes();
        List<ToppingDto> toppings = optionService.getAllToppings();
        Map<String, Object> stats = optionService.getOptionStatistics();

        model.addAttribute("sizes", sizes);
        model.addAttribute("toppings", toppings);
        model.addAttribute("stats", stats);
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("newSize", new ProductSizeRequest());
        model.addAttribute("newTopping", new ToppingRequest());
        model.addAttribute("pageTitle", "Quản Lý Kích Cỡ & Topping");

        return "admin/product-options/index";
    }

    // ==================== PRODUCT SIZE ====================

    @PostMapping("/size/create")
    public String createSize(
            @Valid @ModelAttribute("newSize") ProductSizeRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/product-options?activeTab=size";
        }

        try {
            optionService.createSize(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm kích cỡ '" + request.getSizeName() + "' thành công!");
        } catch (Exception e) {
            log.error(">>> Lỗi khi thêm kích cỡ mới: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Lỗi khi lưu kích cỡ.");
        }
        return "redirect:/admin/product-options?activeTab=size";
    }

    @PostMapping("/size/update/{code}")
    public String updateSize(
            @PathVariable String code,
            @Valid @ModelAttribute ProductSizeRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/product-options?activeTab=size";
        }

        try {
            optionService.updateSize(code, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật kích cỡ '" + request.getSizeName() + "' thành công!");
        } catch (Exception e) {
            log.error(">>> Lỗi khi cập nhật kích cỡ [{}]: ", code, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Lỗi khi cập nhật kích cỡ.");
        }
        return "redirect:/admin/product-options?activeTab=size";
    }

    @PostMapping("/size/toggle/{code}")
    public Object toggleSize(
            @PathVariable String code,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean active = optionService.toggleSizeStatus(code);
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith) || (acceptHeader != null && acceptHeader.contains("application/json"))) {
                return ResponseEntity.ok(Map.of("success", true, "active", active, "message",
                        active ? "Đã mở bán kích cỡ '" + code + "'!" : "Đã tạm dừng kích cỡ '" + code + "'!"));
            }
            redirectAttributes.addFlashAttribute("successMessage",
                    active ? "Đã mở bán kích cỡ '" + code + "'!" : "Đã tạm dừng kích cỡ '" + code + "'!");
        } catch (Exception e) {
            log.error(">>> Lỗi khi chuyển trạng thái kích cỡ [{}]: ", code, e);
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith) || (acceptHeader != null && acceptHeader.contains("application/json"))) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage() != null ? e.getMessage() : "Lỗi xử lý"));
            }
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
            log.error(">>> Lỗi khi xóa kích cỡ [{}]: ", code, e);
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
            redirectAttributes.addFlashAttribute("successMessage", "Thêm topping '" + request.getToppingName() + "' thành công!");
        } catch (Exception e) {
            log.error(">>> Lỗi khi thêm topping mới: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Lỗi khi lưu topping.");
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
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật topping '" + request.getToppingName() + "' thành công!");
        } catch (Exception e) {
            log.error(">>> Lỗi khi cập nhật topping [ID: {}]: ", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Lỗi khi cập nhật topping.");
        }
        return "redirect:/admin/product-options?activeTab=topping";
    }

    @PostMapping("/topping/toggle/{id}")
    public Object toggleTopping(
            @PathVariable Long id,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean active = optionService.toggleToppingStatus(id);
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith) || (acceptHeader != null && acceptHeader.contains("application/json"))) {
                return ResponseEntity.ok(Map.of("success", true, "active", active, "message",
                        active ? "Đã mở bán topping!" : "Đã tạm dừng topping!"));
            }
            redirectAttributes.addFlashAttribute("successMessage",
                    active ? "Đã mở bán topping!" : "Đã tạm dừng topping!");
        } catch (Exception e) {
            log.error(">>> Lỗi khi chuyển trạng thái topping [ID: {}]: ", id, e);
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith) || (acceptHeader != null && acceptHeader.contains("application/json"))) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage() != null ? e.getMessage() : "Lỗi xử lý"));
            }
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
            log.error(">>> Lỗi khi xóa topping [ID: {}]: ", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/product-options?activeTab=topping";
    }
}
