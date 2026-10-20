package com.ute.trasua.repository;

import com.ute.trasua.entity.Ingredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Ingredient (Task 4.1 / Module 14).
 * Manages raw materials and packaging materials across the chain.
 */
@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    List<Ingredient> findByStatus(String status);

    @Query("SELECT i FROM Ingredient i WHERE i.status IS NULL OR UPPER(i.status) = 'ACTIVE' ORDER BY i.id ASC")
    List<Ingredient> findActiveIngredients();

    @Query("SELECT i FROM Ingredient i WHERE " +
           "(:keyword IS NULL OR LOWER(i.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR i.status = :status) " +
           "ORDER BY i.id ASC")
    List<Ingredient> searchIngredients(@Param("keyword") String keyword,
                                       @Param("status") String status);

    @Query("SELECT i FROM Ingredient i WHERE " +
           "(:keyword IS NULL OR LOWER(i.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR i.status = :status) " +
           "ORDER BY i.id ASC")
    Page<Ingredient> searchIngredientsPaged(@Param("keyword") String keyword,
                                            @Param("status") String status,
                                            Pageable pageable);
}
