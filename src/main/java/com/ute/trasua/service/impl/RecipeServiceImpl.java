package com.ute.trasua.service.impl;

import com.ute.trasua.dto.RecipeItemDto;
import com.ute.trasua.entity.Ingredient;
import com.ute.trasua.entity.Product;
import com.ute.trasua.entity.ProductRecipe;
import com.ute.trasua.repository.IngredientRepository;
import com.ute.trasua.repository.ProductRecipeRepository;
import com.ute.trasua.repository.ProductRepository;
import com.ute.trasua.service.RecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeServiceImpl implements RecipeService {

    private final ProductRecipeRepository productRecipeRepository;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RecipeItemDto> getRecipesByProductId(Long productId) {
        if (productId == null) return new ArrayList<>();
        return productRecipeRepository.findByProductIdWithIngredient(productId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveProductRecipes(Long productId, List<RecipeItemDto> recipeItems) {
        if (productId == null) return;
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món với ID: " + productId));

        // Xóa công thức cũ
        productRecipeRepository.deleteByProductId(productId);

        if (recipeItems == null || recipeItems.isEmpty()) {
            log.info("Món ID {} không có công thức nguyên liệu nào.", productId);
            return;
        }

        List<ProductRecipe> toSave = new ArrayList<>();
        for (RecipeItemDto item : recipeItems) {
            if (item.getIngredientId() == null || item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            Ingredient ingredient = ingredientRepository.findById(item.getIngredientId()).orElse(null);
            if (ingredient == null) {
                log.warn("Bỏ qua nguyên liệu không tồn tại ID: {}", item.getIngredientId());
                continue;
            }

            ProductRecipe recipe = ProductRecipe.builder()
                    .product(product)
                    .ingredient(ingredient)
                    .quantity(item.getQuantity())
                    .note(item.getNote() != null ? item.getNote().trim() : null)
                    .build();
            toSave.add(recipe);
        }

        if (!toSave.isEmpty()) {
            productRecipeRepository.saveAll(toSave);
            log.info("Đã lưu {} nguyên liệu trong công thức cho món '{}' (ID: {})", toSave.size(), product.getName(), productId);
        }
    }

    @Override
    @Transactional
    public void deleteRecipesByProductId(Long productId) {
        if (productId != null) {
            productRecipeRepository.deleteByProductId(productId);
        }
    }

    @Override
    public List<RecipeItemDto> parseRecipeInputs(List<Long> ingredientIds, List<BigDecimal> quantities, List<String> notes) {
        List<RecipeItemDto> list = new ArrayList<>();
        if (ingredientIds == null || quantities == null) {
            return list;
        }

        int count = Math.min(ingredientIds.size(), quantities.size());
        for (int i = 0; i < count; i++) {
            Long ingId = ingredientIds.get(i);
            BigDecimal qty = quantities.get(i);
            String note = (notes != null && i < notes.size()) ? notes.get(i) : null;

            if (ingId != null && qty != null && qty.compareTo(BigDecimal.ZERO) > 0) {
                list.add(RecipeItemDto.builder()
                        .ingredientId(ingId)
                        .quantity(qty)
                        .note(note)
                        .build());
            }
        }
        return list;
    }

    private RecipeItemDto toDto(ProductRecipe entity) {
        Ingredient ing = entity.getIngredient();
        return RecipeItemDto.builder()
                .id(entity.getId())
                .ingredientId(ing != null ? ing.getId() : null)
                .ingredientCode(ing != null ? ing.getCode() : "")
                .ingredientName(ing != null ? ing.getName() : "")
                .ingredientUnit(ing != null ? ing.getUnit() : "")
                .quantity(entity.getQuantity())
                .note(entity.getNote())
                .build();
    }
}
