package com.ute.trasua.repository;

import com.ute.trasua.entity.ProductRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRecipeRepository extends JpaRepository<ProductRecipe, Long> {

    List<ProductRecipe> findByProductId(Long productId);

    @Query("SELECT pr FROM ProductRecipe pr JOIN FETCH pr.ingredient WHERE pr.product.id = :productId")
    List<ProductRecipe> findByProductIdWithIngredient(@Param("productId") Long productId);

    void deleteByProductId(Long productId);

    List<ProductRecipe> findByIngredientId(Long ingredientId);
}
