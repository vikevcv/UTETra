package com.ute.trasua.repository;

import com.ute.trasua.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByStatusOrderByNameAsc(String status);

    List<Product> findByCategoryIdAndStatusOrderByNameAsc(Long categoryId, String status);

    List<Product> findByCategoryId(Long categoryId);

    Optional<Product> findByProductCode(String productCode);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByProductCode(String productCode);

    boolean existsByProductCodeAndIdNot(String productCode, Long id);

    long countByCategoryId(Long categoryId);

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category c " +
           "WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:status IS NULL OR p.status = :status) " +
           "ORDER BY c.displayOrder ASC, p.name ASC")
    Page<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("categoryId") Long categoryId,
                                 @Param("status") String status,
                                 Pageable pageable);
}
