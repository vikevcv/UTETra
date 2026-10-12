package com.ute.trasua.service;

import com.ute.trasua.dto.ProductOptionDto;
import com.ute.trasua.dto.ProductSizeDto;
import com.ute.trasua.dto.ProductSizeRequest;
import com.ute.trasua.dto.ToppingDto;
import com.ute.trasua.dto.ToppingRequest;
import com.ute.trasua.entity.ProductOptionType;

import java.util.List;
import java.util.Map;

/**
 * Service managing cup sizes and toppings chain-wide (Module 5 - UC23).
 * Maps to tables 'kich_co' and 'topping' in the SE project ERD.
 */
public interface ProductOptionService {

    // ==================== PRODUCT SIZE ====================
    List<ProductSizeDto> getAllSizes();

    ProductSizeDto getSizeByCode(String code);

    ProductSizeDto createSize(ProductSizeRequest request);

    ProductSizeDto updateSize(String code, ProductSizeRequest request);

    boolean toggleSizeStatus(String code);

    void deleteSize(String code);

    // ==================== TOPPING ====================
    List<ToppingDto> getAllToppings();

    ToppingDto getToppingById(Long id);

    ToppingDto createTopping(ToppingRequest request);

    ToppingDto updateTopping(Long id, ToppingRequest request);

    boolean toggleToppingStatus(Long id);

    void deleteTopping(Long id);

    // ==================== AGGREGATE & API ====================
    List<ProductOptionDto> getActiveOptionsByType(ProductOptionType type);

    Map<String, Object> getOptionStatistics();
}
