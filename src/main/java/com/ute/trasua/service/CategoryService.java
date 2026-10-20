package com.ute.trasua.service;

import com.ute.trasua.dto.CategoryRequest;
import com.ute.trasua.dto.CategoryResponse;
import com.ute.trasua.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for beverage/food Category management (UC22).
 */
public interface CategoryService {

    List<CategoryResponse> getAllCategories();

    List<CategoryResponse> getActiveCategories();

    Page<CategoryResponse> searchCategories(String keyword, Boolean active, Pageable pageable);

    CategoryResponse getCategoryById(Long id);

    Category getCategoryEntityById(Long id);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    boolean toggleCategoryStatus(Long id);

    void deleteCategory(Long id);
}
