package com.ute.trasua.service.impl;

import com.ute.trasua.dto.ProductOptionDto;
import com.ute.trasua.dto.ProductOptionRequest;
import com.ute.trasua.entity.ProductOption;
import com.ute.trasua.entity.ProductOptionType;
import com.ute.trasua.repository.ProductOptionRepository;
import com.ute.trasua.service.ProductOptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Triển khai nghiệp vụ quản lý tùy chọn Size và Topping toàn chuỗi (UC23).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductOptionServiceImpl implements ProductOptionService {

    private final ProductOptionRepository optionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductOptionDto> getAllOptions(ProductOptionType type, Boolean active, String keyword) {
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return optionRepository.searchOptions(type, active, cleanKeyword).stream()
                .map(ProductOptionDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductOptionDto> getActiveOptionsByType(ProductOptionType type) {
        return optionRepository.findByTypeAndActiveTrueOrderByAdditionalPriceAsc(type).stream()
                .map(ProductOptionDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductOptionDto getOptionById(Long id) {
        ProductOption option = optionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tùy chọn với ID: " + id));
        return ProductOptionDto.fromEntity(option);
    }

    @Override
    @Transactional
    public ProductOptionDto createOption(ProductOptionRequest request) {
        String cleanCode = request.getOptionCode().trim().toUpperCase();
        if (optionRepository.existsByOptionCode(cleanCode)) {
            throw new IllegalArgumentException("Mã tùy chọn '" + cleanCode + "' đã tồn tại trong hệ thống");
        }

        ProductOption option = ProductOption.builder()
                .optionCode(cleanCode)
                .name(request.getName().trim())
                .type(request.getType())
                .additionalPrice(request.getAdditionalPrice())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        ProductOption saved = optionRepository.save(option);
        log.info("Chủ chuỗi (Admin) đã tạo tùy chọn mới: {} ({})", saved.getName(), saved.getOptionCode());
        return ProductOptionDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public ProductOptionDto updateOption(Long id, ProductOptionRequest request) {
        ProductOption option = optionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tùy chọn với ID: " + id));

        String cleanCode = request.getOptionCode().trim().toUpperCase();
        if (optionRepository.existsByOptionCodeAndIdNot(cleanCode, id)) {
            throw new IllegalArgumentException("Mã tùy chọn '" + cleanCode + "' đã được sử dụng bởi tùy chọn khác");
        }

        option.setOptionCode(cleanCode);
        option.setName(request.getName().trim());
        option.setType(request.getType());
        option.setAdditionalPrice(request.getAdditionalPrice());
        option.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        if (request.getActive() != null) {
            option.setActive(request.getActive());
        }

        ProductOption updated = optionRepository.save(option);
        log.info("Chủ chuỗi (Admin) đã cập nhật tùy chọn: ID {} ({})", id, updated.getName());
        return ProductOptionDto.fromEntity(updated);
    }

    @Override
    @Transactional
    public boolean toggleActiveStatus(Long id) {
        ProductOption option = optionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tùy chọn với ID: " + id));
        boolean newStatus = !option.isActive();
        option.setActive(newStatus);
        optionRepository.save(option);
        log.info("Tùy chọn ID {} đã đổi trạng thái sang: {}", id, newStatus ? "Đang bán" : "Ngừng bán");
        return newStatus;
    }

    @Override
    @Transactional
    public void deleteOption(Long id) {
        if (!optionRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy tùy chọn với ID: " + id);
        }
        optionRepository.deleteById(id);
        log.info("Đã xóa tùy chọn ID {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOptionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        long total = optionRepository.count();
        long activeSizes = optionRepository.countByTypeAndActiveTrue(ProductOptionType.SIZE);
        long activeToppings = optionRepository.countByTypeAndActiveTrue(ProductOptionType.TOPPING);
        stats.put("totalOptions", total);
        stats.put("activeSizes", activeSizes);
        stats.put("activeToppings", activeToppings);
        stats.put("pausedOptions", total - (activeSizes + activeToppings));
        return stats;
    }
}
