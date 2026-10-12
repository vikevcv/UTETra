package com.ute.trasua.service.impl;

import com.ute.trasua.dto.ProductOptionDto;
import com.ute.trasua.dto.ProductSizeDto;
import com.ute.trasua.dto.ProductSizeRequest;
import com.ute.trasua.dto.ToppingDto;
import com.ute.trasua.dto.ToppingRequest;
import com.ute.trasua.entity.ProductOptionType;
import com.ute.trasua.entity.ProductSize;
import com.ute.trasua.entity.Topping;
import com.ute.trasua.repository.ProductSizeRepository;
import com.ute.trasua.repository.ToppingRepository;
import com.ute.trasua.service.ProductOptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductOptionServiceImpl implements ProductOptionService {

    private final ProductSizeRepository productSizeRepository;
    private final ToppingRepository toppingRepository;

    // ==================== PRODUCT SIZE ====================

    @Override
    @Transactional(readOnly = true)
    public List<ProductSizeDto> getAllSizes() {
        return productSizeRepository.findAllOrderByAdditionalPriceAsc()
                .stream()
                .map(ProductSizeDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductSizeDto getSizeByCode(String code) {
        if (code == null) return null;
        ProductSize size = productSizeRepository.findById(code.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kích cỡ với mã: " + code));
        return ProductSizeDto.fromEntity(size);
    }

    @Override
    @Transactional
    public ProductSizeDto createSize(ProductSizeRequest request) {
        String cleanCode = request.getSizeCode().trim().toUpperCase();
        String cleanName = request.getSizeName().trim();

        if (productSizeRepository.existsBySizeCode(cleanCode)) {
            throw new IllegalArgumentException("Mã kích cỡ '" + cleanCode + "' đã tồn tại trong hệ thống.");
        }
        if (productSizeRepository.existsBySizeName(cleanName)) {
            throw new IllegalArgumentException("Tên kích cỡ '" + cleanName + "' đã được sử dụng.");
        }

        ProductSize size = ProductSize.builder()
                .sizeCode(cleanCode)
                .sizeName(cleanName)
                .additionalPrice(request.getAdditionalPrice())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();

        size = productSizeRepository.save(size);
        log.info(">>> Added product size: [{} - {}], Additional price: {}", size.getSizeCode(), size.getSizeName(), size.getAdditionalPrice());
        return ProductSizeDto.fromEntity(size);
    }

    @Override
    @Transactional
    public ProductSizeDto updateSize(String code, ProductSizeRequest request) {
        String cleanCode = code.trim().toUpperCase();
        ProductSize size = productSizeRepository.findById(cleanCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kích cỡ với mã: " + code));

        String cleanName = request.getSizeName().trim();
        if (productSizeRepository.existsBySizeNameAndSizeCodeNot(cleanName, cleanCode)) {
            throw new IllegalArgumentException("Tên kích cỡ '" + cleanName + "' đã trùng với kích cỡ khác.");
        }

        size.setSizeName(cleanName);
        size.setAdditionalPrice(request.getAdditionalPrice());
        if (request.getStatus() != null) {
            size.setStatus(request.getStatus());
        }

        size = productSizeRepository.save(size);
        log.info(">>> Updated product size: [{} - {}], Additional price: {}", size.getSizeCode(), size.getSizeName(), size.getAdditionalPrice());
        return ProductSizeDto.fromEntity(size);
    }

    @Override
    @Transactional
    public boolean toggleSizeStatus(String code) {
        String cleanCode = code.trim().toUpperCase();
        ProductSize size = productSizeRepository.findById(cleanCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kích cỡ với mã: " + code));

        boolean willActive = !size.isActive();
        size.setStatus(willActive ? "ACTIVE" : "INACTIVE");
        productSizeRepository.save(size);
        return willActive;
    }

    @Override
    @Transactional
    public void deleteSize(String code) {
        String cleanCode = code.trim().toUpperCase();
        if (!productSizeRepository.existsById(cleanCode)) {
            throw new IllegalArgumentException("Không tìm thấy kích cỡ để xóa: " + code);
        }
        productSizeRepository.deleteById(cleanCode);
    }

    // ==================== TOPPING ====================

    @Override
    @Transactional(readOnly = true)
    public List<ToppingDto> getAllToppings() {
        return toppingRepository.findAllOrderByPriceAsc()
                .stream()
                .map(ToppingDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ToppingDto getToppingById(Long id) {
        Topping topping = toppingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy topping với ID: " + id));
        return ToppingDto.fromEntity(topping);
    }

    @Override
    @Transactional
    public ToppingDto createTopping(ToppingRequest request) {
        String cleanName = request.getToppingName().trim();
        if (toppingRepository.existsByToppingName(cleanName)) {
            throw new IllegalArgumentException("Tên topping '" + cleanName + "' đã tồn tại trong hệ thống.");
        }

        Topping topping = Topping.builder()
                .toppingName(cleanName)
                .price(request.getPrice())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();

        topping = toppingRepository.save(topping);
        log.info(">>> Added topping: [ID: {} - {}], Price: {}", topping.getId(), topping.getToppingName(), topping.getPrice());
        return ToppingDto.fromEntity(topping);
    }

    @Override
    @Transactional
    public ToppingDto updateTopping(Long id, ToppingRequest request) {
        Topping topping = toppingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy topping với ID: " + id));

        String cleanName = request.getToppingName().trim();
        if (toppingRepository.existsByToppingNameAndIdNot(cleanName, id)) {
            throw new IllegalArgumentException("Tên topping '" + cleanName + "' đã trùng với loại topping khác.");
        }

        topping.setToppingName(cleanName);
        topping.setPrice(request.getPrice());
        if (request.getStatus() != null) {
            topping.setStatus(request.getStatus());
        }

        topping = toppingRepository.save(topping);
        log.info(">>> Updated topping: [ID: {} - {}], Price: {}", topping.getId(), topping.getToppingName(), topping.getPrice());
        return ToppingDto.fromEntity(topping);
    }

    @Override
    @Transactional
    public boolean toggleToppingStatus(Long id) {
        Topping topping = toppingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy topping với ID: " + id));

        boolean willActive = !topping.isActive();
        topping.setStatus(willActive ? "ACTIVE" : "INACTIVE");
        toppingRepository.save(topping);
        return willActive;
    }

    @Override
    @Transactional
    public void deleteTopping(Long id) {
        if (!toppingRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy topping để xóa với ID: " + id);
        }
        toppingRepository.deleteById(id);
    }

    // ==================== AGGREGATE & API ====================

    @Override
    @Transactional(readOnly = true)
    public List<ProductOptionDto> getActiveOptionsByType(ProductOptionType type) {
        if (type == ProductOptionType.SIZE) {
            return productSizeRepository.findActiveOrderByAdditionalPriceAsc()
                    .stream()
                    .map(ProductOptionDto::fromProductSize)
                    .toList();
        } else if (type == ProductOptionType.TOPPING) {
            return toppingRepository.findActiveOrderByPriceAsc()
                    .stream()
                    .map(ProductOptionDto::fromTopping)
                    .toList();
        }
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOptionStatistics() {
        long totalSizes = productSizeRepository.count();
        long activeSizes = productSizeRepository.findActiveOrderByAdditionalPriceAsc().size();
        long totalToppings = toppingRepository.count();
        long activeToppings = toppingRepository.findActiveOrderByPriceAsc().size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSizes", totalSizes);
        stats.put("activeSizes", activeSizes);
        stats.put("totalToppings", totalToppings);
        stats.put("activeToppings", activeToppings);
        stats.put("total", totalSizes + totalToppings);
        stats.put("activeTotal", activeSizes + activeToppings);
        return stats;
    }
}
