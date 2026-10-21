package com.ute.trasua.service.impl;

import com.ute.trasua.dto.ProductOptionDto;
import com.ute.trasua.dto.ProductSizeDto;
import com.ute.trasua.dto.ProductSizeRequest;
import com.ute.trasua.dto.ToppingDto;
import com.ute.trasua.dto.ToppingRequest;
import com.ute.trasua.entity.ProductOptionType;
import com.ute.trasua.entity.ProductSize;
import com.ute.trasua.entity.Size;
import com.ute.trasua.entity.Topping;
import com.ute.trasua.repository.ProductSizeRepository;
import com.ute.trasua.repository.SizeRepository;
import com.ute.trasua.repository.ToppingRepository;
import com.ute.trasua.service.ProductOptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductOptionServiceImpl implements ProductOptionService {

    private final ProductSizeRepository productSizeRepository;
    private final ToppingRepository toppingRepository;
    private final SizeRepository sizeRepository;

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

        LocalDateTime now = LocalDateTime.now();
        ProductSize size = ProductSize.builder()
                .sizeCode(cleanCode)
                .sizeName(cleanName)
                .additionalPrice(request.getAdditionalPrice())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();

        size = productSizeRepository.save(size);

        // Đồng bộ sang bảng sizes (dùng cho POS & OrderDetail)
        syncSizeToLegacyTable(cleanCode, cleanName, request.getAdditionalPrice(), "ACTIVE".equalsIgnoreCase(size.getStatus()));

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
        size.setUpdatedAt(LocalDateTime.now());

        size = productSizeRepository.save(size);

        // Đồng bộ cập nhật sang bảng sizes
        syncSizeToLegacyTable(cleanCode, cleanName, request.getAdditionalPrice(), size.isActive());

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
        size.setUpdatedAt(LocalDateTime.now());
        productSizeRepository.save(size);

        // Đồng bộ toggle sang bảng sizes
        try {
            sizeRepository.findByName(cleanCode).ifPresent(s -> {
                s.setActive(willActive);
                sizeRepository.save(s);
            });
        } catch (Exception ex) {
            log.warn("Could not sync toggle to sizes table: {}", ex.getMessage());
        }

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

        // Đồng bộ xóa sang bảng sizes nếu có
        try {
            sizeRepository.findByName(cleanCode).ifPresent(sizeRepository::delete);
        } catch (Exception ex) {
            log.warn("Could not sync delete to sizes table: {}", ex.getMessage());
        }
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

        LocalDateTime now = LocalDateTime.now();
        Topping topping = Topping.builder()
                .toppingName(cleanName)
                .price(request.getPrice())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .createdAt(now)
                .updatedAt(now)
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
        topping.setUpdatedAt(LocalDateTime.now());

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
        topping.setUpdatedAt(LocalDateTime.now());
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

    // ==================== HELPER METHODS ====================

    private void syncSizeToLegacyTable(String code, String name, java.math.BigDecimal price, boolean active) {
        if (sizeRepository == null) return;
        try {
            sizeRepository.findByName(code).ifPresentOrElse(
                    existing -> {
                        existing.setExtraPrice(price);
                        existing.setActive(active);
                        Integer vol = parseVolume(name);
                        if (vol != null) existing.setVolumeMl(vol);
                        sizeRepository.save(existing);
                    },
                    () -> {
                        Integer vol = parseVolume(name);
                        sizeRepository.save(Size.builder()
                                .name(code)
                                .volumeMl(vol)
                                .extraPrice(price)
                                .active(active)
                                .build());
                    }
            );
        } catch (Exception ex) {
            log.warn("Could not sync size to legacy sizes table: {}", ex.getMessage());
        }
    }

    private Integer parseVolume(String name) {
        if (name == null) return null;
        Matcher m = Pattern.compile("(\\d+)\\s*ml", Pattern.CASE_INSENSITIVE).matcher(name);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (Exception ignored) {}
        }
        return null;
    }
}
