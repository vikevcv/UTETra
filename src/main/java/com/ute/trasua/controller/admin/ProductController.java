package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.CategoryResponse;
import com.ute.trasua.dto.ProductBranchMatrixRow;
import com.ute.trasua.dto.ProductRequest;
import com.ute.trasua.dto.ProductResponse;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.service.BranchMenuService;
import com.ute.trasua.service.CategoryService;
import com.ute.trasua.service.ProductService;
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
 * Controller serving Chain Admin managing beverage and food menu items (Module 13 - UC22).
 */
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BranchMenuService branchMenuService;
    private final BranchRepository branchRepository;
    private final com.ute.trasua.service.IngredientService ingredientService;
    private final com.ute.trasua.service.RecipeService recipeService;

    /**
     * Display list of products with search, category filtering, status filtering, and pagination.
     */
    @GetMapping
    public String listProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        Page<ProductResponse> productsPage = productService.searchProducts(
                cleanKeyword,
                categoryId,
                cleanStatus,
                PageRequest.of(page, size, Sort.by("id").descending())
        );

        List<ProductResponse> allProducts = productService.getAllProducts();
        long totalProducts = allProducts.size();
        long activeProducts = allProducts.stream().filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus())).count();
        long inactiveProducts = totalProducts - activeProducts;

        List<CategoryResponse> categories = categoryService.getAllCategories();
        long totalCategories = categories.size();

        model.addAttribute("productsPage", productsPage);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("inactiveProducts", inactiveProducts);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("pageTitle", "Quản Lý Thực Đơn & Món Nước — UTETra");

        return "admin/products/list";
    }

    /**
     * Display create product form.
     */
    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new ProductRequest());
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("ingredients", ingredientService.getActiveIngredients());
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Thêm Món Nước Mới — UTETra");
        return "admin/products/form";
    }

    /**
     * Process create product form submission with Cloudinary image upload.
     */
    @PostMapping("/create")
    public String createProduct(
            @ModelAttribute("product") ProductRequest request,
            @RequestParam(value = "recipeIngredientIds", required = false) List<Long> recipeIngredientIds,
            @RequestParam(value = "recipeQuantities", required = false) List<java.math.BigDecimal> recipeQuantities,
            @RequestParam(value = "recipeNotes", required = false) List<String> recipeNotes,
            RedirectAttributes redirectAttributes
    ) {
        try {
            request.setRecipeIngredientIds(recipeIngredientIds);
            request.setRecipeQuantities(recipeQuantities);
            request.setRecipeNotes(recipeNotes);

            ProductResponse created = productService.createProduct(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thêm món nước [" + created.getName() + "] thành công!");
            return "redirect:/admin/products";
        } catch (Exception e) {
            log.error(">>> Error creating product: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products/new";
        }
    }

    /**
     * Display edit product form.
     */
    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductResponse res = productService.getProductById(id);
            ProductRequest form = ProductRequest.builder()
                    .id(res.getId())
                    .productCode(res.getProductCode())
                    .name(res.getName())
                    .categoryId(res.getCategoryId())
                    .basePrice(res.getBasePrice())
                    .description(res.getDescription())
                    .status(res.getStatus())
                    .currentImageUrl(res.getImageUrl())
                    .build();

            model.addAttribute("product", form);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("ingredients", ingredientService.getActiveIngredients());
            model.addAttribute("recipes", recipeService.getRecipesByProductId(id));
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Chỉnh Sửa Món: " + res.getName() + " — UTETra");
            return "admin/products/form";
        } catch (Exception e) {
            log.error(">>> Error fetching product ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products";
        }
    }

    /**
     * Process update product form submission with optional replacement image upload.
     */
    @PostMapping("/{id}/update")
    public String updateProduct(
            @PathVariable("id") Long id,
            @ModelAttribute("product") ProductRequest request,
            @RequestParam(value = "recipeIngredientIds", required = false) List<Long> recipeIngredientIds,
            @RequestParam(value = "recipeQuantities", required = false) List<java.math.BigDecimal> recipeQuantities,
            @RequestParam(value = "recipeNotes", required = false) List<String> recipeNotes,
            RedirectAttributes redirectAttributes
    ) {
        try {
            request.setRecipeIngredientIds(recipeIngredientIds);
            request.setRecipeQuantities(recipeQuantities);
            request.setRecipeNotes(recipeNotes);

            ProductResponse updated = productService.updateProduct(id, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật món [" + updated.getName() + "] thành công!");
            return "redirect:/admin/products";
        } catch (Exception e) {
            log.error(">>> Error updating product ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products/" + id + "/edit";
        }
    }

    /**
     * Toggle active/inactive status of a product via AJAX.
     */
    @PostMapping("/{id}/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleStatus(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean active = productService.toggleProductStatus(id);
            response.put("success", true);
            response.put("active", active);
            response.put("status", active ? "ACTIVE" : "INACTIVE");
            response.put("message", active ? "Đã chuyển sang trạng thái Đang Bán!" : "Đã tạm dừng kinh doanh món này!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Delete a product.
     */
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa món nước thành công!");
        } catch (Exception e) {
            log.error(">>> Error deleting product ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    /**
     * REST endpoint to get product details as JSON.
     */
    @GetMapping("/{id}/json")
    @ResponseBody
    public ResponseEntity<ProductResponse> getProductJson(@PathVariable("id") Long id) {
        try {
            ProductResponse response = productService.getProductById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // =========================================================================
    // BRANCH MENU AVAILABILITY MATRIX (TASK 3.6 - UC22)
    // =========================================================================

    /**
     * Display Matrix of Products x Branches to toggle in-stock/out-of-stock per branch.
     */
    @GetMapping("/branch-status")
    public String branchStatusMatrix(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            Model model
    ) {
        List<Branch> branches = branchRepository.findByActiveTrue();
        List<ProductBranchMatrixRow> matrixRows = branchMenuService.getProductBranchMatrix(keyword, categoryId);
        List<CategoryResponse> categories = categoryService.getAllCategories();

        model.addAttribute("branches", branches);
        model.addAttribute("matrixRows", matrixRows);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("totalBranches", branches.size());
        model.addAttribute("totalProducts", matrixRows.size());
        model.addAttribute("pageTitle", "Quản Lý Trạng Thái Món Theo Chi Nhánh — UTETra");

        return "admin/products/branch-status";
    }

    /**
     * AJAX endpoint to toggle in-stock / out-of-stock for a product at a specific branch.
     */
    @PostMapping("/branch-status/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleBranchProductStatus(
            @RequestParam("branchId") Long branchId,
            @RequestParam("productId") Long productId,
            @RequestParam("available") boolean available
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            boolean updated = branchMenuService.updateProductAvailability(branchId, productId, available);
            res.put("success", true);
            res.put("available", updated);
            res.put("message", updated ? "Đã chuyển sang trạng thái [Còn hàng] tại chi nhánh!" : "Đã chuyển sang trạng thái [Hết hàng / Tạm ngưng] tại chi nhánh!");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error(">>> Error toggling branch product status: {}", e.getMessage());
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    /**
     * Sync all products into all active branches.
     */
    @PostMapping("/branch-status/sync-all")
    public String syncAllBranches(RedirectAttributes redirectAttributes) {
        try {
            branchMenuService.syncAllProductsToAllBranches();
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã đồng bộ thực đơn đầy đủ vào tất cả chi nhánh trong chuỗi!");
        } catch (Exception e) {
            log.error(">>> Error syncing all branches: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/products/branch-status";
    }

    /**
     * Sync all products into a specific branch.
     */
    @PostMapping("/branch-status/sync-branch")
    public String syncSpecificBranch(@RequestParam("branchId") Long branchId, RedirectAttributes redirectAttributes) {
        try {
            branchMenuService.syncAllProductsToBranch(branchId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã đồng bộ toàn bộ món vào thực đơn của chi nhánh!");
        } catch (Exception e) {
            log.error(">>> Error syncing branch ID {}: {}", branchId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/products/branch-status";
    }
}
