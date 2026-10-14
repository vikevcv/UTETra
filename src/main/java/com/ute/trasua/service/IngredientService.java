package com.ute.trasua.service;

import com.ute.trasua.dto.IngredientRequest;
import com.ute.trasua.dto.IngredientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Ingredient management (Task 4.2 / Module 14).
 */
public interface IngredientService {

    List<IngredientResponse> getAllIngredients();

    List<IngredientResponse> getActiveIngredients();

    Page<IngredientResponse> searchIngredientsPaged(String keyword, String status, Pageable pageable);

    IngredientResponse getIngredientById(Long id);

    IngredientResponse getIngredientByCode(String code);

    IngredientResponse createIngredient(IngredientRequest request);

    IngredientResponse updateIngredient(Long id, IngredientRequest request);

    void deleteIngredient(Long id);
}
