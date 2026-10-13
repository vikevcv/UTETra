package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.CategoryRequest;
import com.ute.trasua.dto.CategoryResponse;
import com.ute.trasua.repository.ProductRepository;
import com.ute.trasua.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller serving Chain Admin managing Beverage and Food Categories (Module 13 - UC22).
 */
@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductRepository productRepository;

    /**
     * Display list of categories with search, filter, and statistics overview.
     */
    @GetMapping
    public String listCategories(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        Page<CategoryResponse> categoriesPage = categoryService.searchCategories(
                keyword,
                active,
                PageRequest.of(page, size, Sort.by("displayOrder").ascending().and(Sort.by("id").ascending()))
        );

        List<CategoryResponse> allCategories = categoryService.getAllCategories();
        long totalCategories = allCategories.size();
        long activeCategories = allCategories.stream().filter(CategoryResponse::isActive).count();
        long inactiveCategories = totalCategories - activeCategories;
        long totalProducts = productRepository.count();

        model.addAttribute("categoriesPage", categoriesPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("active", active);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("activeCategories", activeCategories);
        model.addAttribute("inactiveCategories", inactiveCategories);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("newCategory", new CategoryRequest());
        model.addAttribute("pageTitle", "Quản Lý Danh Mục Món Nước — UTETra");

        return "admin/categories/list";
    }

    /**
     * Create new category via standard form submission.
     */
    @PostMapping("/create")
    public String createCategory(
            @ModelAttribute("newCategory") CategoryRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            categoryService.createCategory(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục [" + request.getName() + "] thành công!");
        } catch (Exception e) {
            log.error(">>> Error creating category: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    /**
     * Update category via standard form submission.
     */
    @PostMapping("/{id}/update")
    public String updateCategory(
            @PathVariable("id") Long id,
            @ModelAttribute("category") CategoryRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            categoryService.updateCategory(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
        } catch (Exception e) {
            log.error(">>> Error updating category ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    /**
     * Toggle active status of a category.
     */
    @PostMapping("/{id}/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleStatus(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean active = categoryService.toggleCategoryStatus(id);
            response.put("success", true);
            response.put("active", active);
            response.put("message", active ? "Đã kích hoạt danh mục!" : "Đã tạm dừng danh mục!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Delete a category.
     */
    @PostMapping("/{id}/delete")
    public String deleteCategory(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục thành công!");
        } catch (Exception e) {
            log.error(">>> Error deleting category ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    /**
     * REST endpoint to get category details as JSON for editing inside modal without page reload.
     */
    @GetMapping("/{id}/json")
    @ResponseBody
    public ResponseEntity<CategoryResponse> getCategoryJson(@PathVariable("id") Long id) {
        try {
            CategoryResponse response = categoryService.getCategoryById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * AJAX endpoint to save or update category asynchronously from modal without reloading.
     */
    @PostMapping("/api/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiSaveCategory(@RequestBody CategoryRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            CategoryResponse saved;
            if (request.getId() != null && request.getId() > 0) {
                saved = categoryService.updateCategory(request.getId(), request);
                result.put("message", "Cập nhật danh mục thành công!");
            } else {
                saved = categoryService.createCategory(request);
                result.put("message", "Thêm danh mục mới thành công!");
            }
            result.put("success", true);
            result.put("data", saved);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
