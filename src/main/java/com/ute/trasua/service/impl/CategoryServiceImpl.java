package com.ute.trasua.service.impl;

import com.ute.trasua.dto.CategoryRequest;
import com.ute.trasua.dto.CategoryResponse;
import com.ute.trasua.entity.Category;
import com.ute.trasua.repository.CategoryRepository;
import com.ute.trasua.repository.ProductRepository;
import com.ute.trasua.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Category management (UC22).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::enrichToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findAllByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::enrichToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategories(String keyword, Boolean active, Pageable pageable) {
        Page<Category> page = categoryRepository.searchCategories(keyword, active, pageable);
        return page.map(this::enrichToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = getCategoryEntityById(id);
        return enrichToResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name must not be empty!");
        }

        String name = request.getName().trim();
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Category name [" + name + "] already exists in the system!");
        }

        Category category = Category.builder()
                .name(name)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .active(request.isActive())
                .build();

        Category saved = categoryRepository.save(category);
        log.info(">>> Created new Category: [{}] {}", saved.getId(), saved.getName());
        return enrichToResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = getCategoryEntityById(id);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String name = request.getName().trim();
            if (categoryRepository.existsByNameAndIdNot(name, id)) {
                throw new IllegalArgumentException("Category name [" + name + "] is already in use!");
            }
            category.setName(name);
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription().trim());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        category.setActive(request.isActive());

        Category updated = categoryRepository.save(category);
        log.info(">>> Updated Category: [{}] {}", updated.getId(), updated.getName());
        return enrichToResponse(updated);
    }

    @Override
    @Transactional
    public boolean toggleCategoryStatus(Long id) {
        Category category = getCategoryEntityById(id);
        category.setActive(!category.isActive());
        categoryRepository.save(category);
        log.info(">>> Toggled Category ID {} active status to: {}", id, category.isActive());
        return category.isActive();
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryEntityById(id);
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException("Cannot delete category [" + category.getName() + "] because it contains " +
                    productCount + " products. Please reassign or delete the products first!");
        }
        categoryRepository.delete(category);
        log.info(">>> Deleted Category ID: [{}] {}", id, category.getName());
    }

    private CategoryResponse enrichToResponse(Category c) {
        long count = productRepository.countByCategoryId(c.getId());
        return CategoryResponse.fromEntity(c, count);
    }
}
