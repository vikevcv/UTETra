package com.ute.trasua.repository;

import com.ute.trasua.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatusOrderByNameAsc(String status);
    List<Product> findByCategoryIdAndStatusOrderByNameAsc(Long categoryId, String status);
    Optional<Product> findByProductCode(String productCode);
    boolean existsByName(String name);
}
