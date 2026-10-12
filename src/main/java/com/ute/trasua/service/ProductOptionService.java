package com.ute.trasua.service;

import com.ute.trasua.dto.ProductOptionDto;
import com.ute.trasua.dto.ProductOptionRequest;
import com.ute.trasua.entity.ProductOptionType;

import java.util.List;
import java.util.Map;

/**
 * Service interface quản lý tùy chọn Size và Topping toàn chuỗi (UC23).
 */
public interface ProductOptionService {

    List<ProductOptionDto> getAllOptions(ProductOptionType type, Boolean active, String keyword);

    List<ProductOptionDto> getActiveOptionsByType(ProductOptionType type);

    ProductOptionDto getOptionById(Long id);

    ProductOptionDto createOption(ProductOptionRequest request);

    ProductOptionDto updateOption(Long id, ProductOptionRequest request);

    boolean toggleActiveStatus(Long id);

    void deleteOption(Long id);

    Map<String, Object> getOptionStatistics();
}
