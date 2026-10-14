package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.IngredientRequest;
import com.ute.trasua.dto.IngredientResponse;
import com.ute.trasua.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for Chain Admin to manage global ingredients and packaging (Task 4.3 / Module 14).
 */
@Controller
@RequestMapping("/admin/ingredients")
@RequiredArgsConstructor
@Slf4j
public class AdminIngredientController {

    private final IngredientService ingredientService;

    @GetMapping
    public String listIngredients(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        Page<IngredientResponse> ingredientsPage = ingredientService.searchIngredientsPaged(
                keyword, status, PageRequest.of(Math.max(0, page), size)
        );

        model.addAttribute("ingredientsPage", ingredientsPage);
        model.addAttribute("ingredients", ingredientsPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("totalIngredients", ingredientsPage.getTotalElements());
        model.addAttribute("pageTitle", "Quản Lý Danh Mục Nguyên Liệu Chuỗi");

        return "admin/inventory/ingredients";
    }

    @PostMapping
    public String createIngredient(
            @Valid @ModelAttribute("ingredientRequest") IngredientRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/ingredients";
        }
        try {
            ingredientService.createIngredient(request);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm mới nguyên liệu '" + request.getName() + "' thành công!");
        } catch (Exception e) {
            log.error("Lỗi tạo mới nguyên liệu: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/ingredients";
    }

    @PostMapping("/{id}/update")
    public String updateIngredient(
            @PathVariable Long id,
            @Valid @ModelAttribute("ingredientRequest") IngredientRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/ingredients";
        }
        try {
            ingredientService.updateIngredient(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật nguyên liệu '" + request.getName() + "' thành công!");
        } catch (Exception e) {
            log.error("Lỗi cập nhật nguyên liệu: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/ingredients";
    }

    @PostMapping("/{id}/delete")
    public String deleteIngredient(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ingredientService.deleteIngredient(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa nguyên liệu thành công!");
        } catch (Exception e) {
            log.error("Lỗi xóa nguyên liệu: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/ingredients";
    }
}
