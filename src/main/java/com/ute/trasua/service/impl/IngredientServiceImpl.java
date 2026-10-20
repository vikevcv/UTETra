package com.ute.trasua.service.impl;

import com.ute.trasua.dto.IngredientRequest;
import com.ute.trasua.dto.IngredientResponse;
import com.ute.trasua.entity.Ingredient;
import com.ute.trasua.repository.BranchInventoryRepository;
import com.ute.trasua.repository.ImportReceiptDetailRepository;
import com.ute.trasua.repository.IngredientRepository;
import com.ute.trasua.service.IngredientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository ingredientRepository;
    private final BranchInventoryRepository branchInventoryRepository;
    private final ImportReceiptDetailRepository importReceiptDetailRepository;

    @Override
    public List<IngredientResponse> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(IngredientResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<IngredientResponse> getActiveIngredients() {
        List<Ingredient> list = ingredientRepository.findActiveIngredients();
        if (list == null || list.isEmpty()) {
            list = ingredientRepository.findAll();
        }
        return list.stream()
                .filter(i -> i.getStatus() == null || !"INACTIVE".equalsIgnoreCase(i.getStatus()))
                .map(IngredientResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<IngredientResponse> searchIngredientsPaged(String keyword, String status, Pageable pageable) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        return ingredientRepository.searchIngredientsPaged(cleanKeyword, cleanStatus, pageable)
                .map(IngredientResponse::fromEntity);
    }

    @Override
    public IngredientResponse getIngredientById(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguyên liệu có ID: " + id));
        return IngredientResponse.fromEntity(ingredient);
    }

    @Override
    public IngredientResponse getIngredientByCode(String code) {
        Ingredient ingredient = ingredientRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguyên liệu có mã: " + code));
        return IngredientResponse.fromEntity(ingredient);
    }

    @Override
    @Transactional
    public IngredientResponse createIngredient(IngredientRequest request) {
        if (ingredientRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Mã nguyên liệu '" + request.getCode() + "' đã tồn tại!");
        }
        if (ingredientRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Tên nguyên liệu '" + request.getName() + "' đã tồn tại!");
        }

        Ingredient ingredient = Ingredient.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .unit(request.getUnit().trim())
                .referencePrice(request.getReferencePrice())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();

        Ingredient saved = ingredientRepository.save(ingredient);
        log.info(">>> Đã tạo mới nguyên liệu: {} ({})", saved.getName(), saved.getCode());
        return IngredientResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public IngredientResponse updateIngredient(Long id, IngredientRequest request) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguyên liệu có ID: " + id));

        // Kiểm tra trùng mã khác ID
        ingredientRepository.findByCode(request.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Mã nguyên liệu '" + request.getCode() + "' đã được sử dụng!");
            }
        });

        ingredient.setCode(request.getCode().trim().toUpperCase());
        ingredient.setName(request.getName().trim());
        ingredient.setUnit(request.getUnit().trim());
        ingredient.setReferencePrice(request.getReferencePrice());
        ingredient.setDescription(request.getDescription());
        ingredient.setImageUrl(request.getImageUrl());
        if (request.getStatus() != null) {
            ingredient.setStatus(request.getStatus());
        }

        Ingredient updated = ingredientRepository.save(ingredient);
        log.info(">>> Đã cập nhật nguyên liệu: {} ({})", updated.getName(), updated.getCode());
        return IngredientResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteIngredient(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguyên liệu có ID: " + id));

        if (importReceiptDetailRepository.existsByIngredientId(id)) {
            throw new IllegalStateException("Không thể xóa nguyên liệu đã có trong phiếu nhập kho lịch sử. Vui lòng chuyển trạng thái INACTIVE!");
        }

        ingredientRepository.delete(ingredient);
        log.info(">>> Đã xóa nguyên liệu ID: {}", id);
    }
}
