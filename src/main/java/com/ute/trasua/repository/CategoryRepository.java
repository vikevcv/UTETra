package com.ute.trasua.repository;

import com.ute.trasua.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByActiveTrueOrderByDisplayOrderAsc();

    List<Category> findAllByOrderByDisplayOrderAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT c FROM Category c WHERE " +
           "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:active IS NULL OR c.active = :active) " +
           "ORDER BY c.displayOrder ASC, c.id ASC")
    Page<Category> searchCategories(@Param("keyword") String keyword,
                                    @Param("active") Boolean active,
                                    Pageable pageable);
}
