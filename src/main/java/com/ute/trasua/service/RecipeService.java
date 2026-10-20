package com.ute.trasua.service;

import com.ute.trasua.dto.RecipeItemDto;

import java.math.BigDecimal;
import java.util.List;

public interface RecipeService {

    List<RecipeItemDto> getRecipesByProductId(Long productId);

    void saveProductRecipes(Long productId, List<RecipeItemDto> recipeItems);

    void deleteRecipesByProductId(Long productId);

    List<RecipeItemDto> parseRecipeInputs(List<Long> ingredientIds, List<BigDecimal> quantities, List<String> notes);
}
