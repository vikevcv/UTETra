package com.ute.trasua.service;

import com.ute.trasua.dto.*;
import com.ute.trasua.entity.ProductOptionType;

import java.util.List;
import java.util.Map;

/**
 * Service quản lý danh mục Kích cỡ (Size) và Topping toàn chuỗi (Module 5 - UC23).
 * Phục vụ cả 2 bảng độc lập KichCo và Topping theo chuẩn ERD Báo Cáo SE.
 */
public interface ProductOptionService {

    // ==================== KÍCH CỠ (SIZE) ====================
    List<SizeDto> getAllSizes();

    SizeDto getSizeByCode(String code);

    SizeDto createSize(SizeRequest request);

    SizeDto updateSize(String code, SizeRequest request);

    boolean toggleSizeStatus(String code);

    void deleteSize(String code);

    // ==================== TOPPING ====================
    List<ToppingDto> getAllToppings();

    ToppingDto getToppingById(Long id);

    ToppingDto createTopping(ToppingRequest request);

    ToppingDto updateTopping(Long id, ToppingRequest request);

    boolean toggleToppingStatus(Long id);

    void deleteTopping(Long id);

    // ==================== TỔNG HỢP & API ====================
    List<ProductOptionDto> getActiveOptionsByType(ProductOptionType type);

    Map<String, Object> getOptionStatistics();
}
