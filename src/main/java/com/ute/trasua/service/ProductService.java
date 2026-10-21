package com.ute.trasua.service;

import com.ute.trasua.dto.ProductRequest;
import com.ute.trasua.dto.ProductResponse;
import com.ute.trasua.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Product/Menu item management (UC22).
 */
public interface ProductService {

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getActiveProducts();

    List<ProductResponse> getProductsByCategory(Long categoryId);

    List<ProductResponse> filterProducts(String keyword, Long categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, String sortBy);

    List<ProductResponse> filterProducts(String keyword, Long categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, String sortBy, Long branchId);

    Page<ProductResponse> searchProducts(String keyword, Long categoryId, String status, Pageable pageable);

    ProductResponse getProductById(Long id);

    ProductResponse getProductById(Long id, Long branchId);

    Product getProductEntityById(Long id);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    boolean toggleProductStatus(Long id);

    void deleteProduct(Long id);

    void syncProductToAllBranches(Product product);
}
