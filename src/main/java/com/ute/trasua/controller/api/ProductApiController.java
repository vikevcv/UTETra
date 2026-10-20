package com.ute.trasua.controller.api;

import com.ute.trasua.dto.ProductResponse;
import com.ute.trasua.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST API phục vụ tìm kiếm & lọc sản phẩm tức thì (AJAX) phía khách hàng.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductApiController {

    private final ProductService productService;

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "sort", required = false, defaultValue = "default") String sort) {

        List<ProductResponse> products = productService.filterProducts(keyword, categoryId, minPrice, maxPrice, sort);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", products.size(),
                "data", products
        ));
    }
}
