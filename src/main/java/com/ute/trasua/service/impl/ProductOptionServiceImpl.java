package com.ute.trasua.service.impl;

import com.ute.trasua.dto.*;
import com.ute.trasua.entity.KichCo;
import com.ute.trasua.entity.ProductOptionType;
import com.ute.trasua.entity.Topping;
import com.ute.trasua.repository.KichCoRepository;
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

    private final KichCoRepository kichCoRepository;
    private final ToppingRepository toppingRepository;

    // ==================== KÍCH CỠ (SIZE) ====================

    @Override
    @Transactional(readOnly = true)
    public List<SizeDto> getAllSizes() {
        return kichCoRepository.findAllOrderByGiaPhuThuAsc()
                .stream()
                .map(SizeDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SizeDto getSizeByCode(String code) {
        if (code == null) return null;
        KichCo size = kichCoRepository.findById(code.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kích cỡ với mã: " + code));
        return SizeDto.fromEntity(size);
    }

    @Override
    @Transactional
    public SizeDto createSize(SizeRequest request) {
        String cleanCode = request.getMaKichCo().trim().toUpperCase();
        String cleanName = request.getTenKichCo().trim();

        if (kichCoRepository.existsByMaKichCo(cleanCode)) {
            throw new IllegalArgumentException("Mã kích cỡ '" + cleanCode + "' đã tồn tại trong hệ thống.");
        }
        if (kichCoRepository.existsByTenKichCo(cleanName)) {
            throw new IllegalArgumentException("Tên kích cỡ '" + cleanName + "' đã được sử dụng.");
        }

        KichCo size = KichCo.builder()
                .maKichCo(cleanCode)
                .tenKichCo(cleanName)
                .giaPhuThu(request.getGiaPhuThu())
                .trangThai(request.getTrangThai() != null ? request.getTrangThai() : "HoatDong")
                .build();

        size = kichCoRepository.save(size);
        log.info(">>> Đã thêm kích cỡ mới: [{} - {}], Phụ thu: {}", size.getMaKichCo(), size.getTenKichCo(), size.getGiaPhuThu());
        return SizeDto.fromEntity(size);
    }

    @Override
    @Transactional
    public SizeDto updateSize(String code, SizeRequest request) {
        String cleanCode = code.trim().toUpperCase();
        KichCo size = kichCoRepository.findById(cleanCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kích cỡ với mã: " + code));

        String cleanName = request.getTenKichCo().trim();
        if (kichCoRepository.existsByTenKichCoAndMaKichCoNot(cleanName, cleanCode)) {
            throw new IllegalArgumentException("Tên kích cỡ '" + cleanName + "' đã trùng với kích cỡ khác.");
        }

        size.setTenKichCo(cleanName);
        size.setGiaPhuThu(request.getGiaPhuThu());
        if (request.getTrangThai() != null) {
            size.setTrangThai(request.getTrangThai());
        }

        size = kichCoRepository.save(size);
        log.info(">>> Đã cập nhật kích cỡ: [{} - {}], Phụ thu: {}", size.getMaKichCo(), size.getTenKichCo(), size.getGiaPhuThu());
        return SizeDto.fromEntity(size);
    }

    @Override
    @Transactional
    public boolean toggleSizeStatus(String code) {
        String cleanCode = code.trim().toUpperCase();
        KichCo size = kichCoRepository.findById(cleanCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kích cỡ với mã: " + code));

        boolean willActive = !size.isHoatDong();
        size.setTrangThai(willActive ? "HoatDong" : "TamNgung");
        kichCoRepository.save(size);
        return willActive;
    }

    @Override
    @Transactional
    public void deleteSize(String code) {
        String cleanCode = code.trim().toUpperCase();
        if (!kichCoRepository.existsById(cleanCode)) {
            throw new IllegalArgumentException("Không tìm thấy kích cỡ để xóa: " + code);
        }
        kichCoRepository.deleteById(cleanCode);
    }

    // ==================== TOPPING ====================

    @Override
    @Transactional(readOnly = true)
    public List<ToppingDto> getAllToppings() {
        return toppingRepository.findAllOrderByDonGiaAsc()
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
        String cleanName = request.getTenTopping().trim();
        if (toppingRepository.existsByTenTopping(cleanName)) {
            throw new IllegalArgumentException("Tên topping '" + cleanName + "' đã tồn tại trong hệ thống.");
        }

        Topping topping = Topping.builder()
                .tenTopping(cleanName)
                .donGia(request.getDonGia())
                .trangThai(request.getTrangThai() != null ? request.getTrangThai() : "HoatDong")
                .build();

        topping = toppingRepository.save(topping);
        log.info(">>> Đã thêm topping mới: [ID: {} - {}], Đơn giá: {}", topping.getMaTopping(), topping.getTenTopping(), topping.getDonGia());
        return ToppingDto.fromEntity(topping);
    }

    @Override
    @Transactional
    public ToppingDto updateTopping(Long id, ToppingRequest request) {
        Topping topping = toppingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy topping với ID: " + id));

        String cleanName = request.getTenTopping().trim();
        if (toppingRepository.existsByTenToppingAndMaToppingNot(cleanName, id)) {
            throw new IllegalArgumentException("Tên topping '" + cleanName + "' đã trùng với loại topping khác.");
        }

        topping.setTenTopping(cleanName);
        topping.setDonGia(request.getDonGia());
        if (request.getTrangThai() != null) {
            topping.setTrangThai(request.getTrangThai());
        }

        topping = toppingRepository.save(topping);
        log.info(">>> Đã cập nhật topping: [ID: {} - {}], Đơn giá: {}", topping.getMaTopping(), topping.getTenTopping(), topping.getDonGia());
        return ToppingDto.fromEntity(topping);
    }

    @Override
    @Transactional
    public boolean toggleToppingStatus(Long id) {
        Topping topping = toppingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy topping với ID: " + id));

        boolean willActive = !topping.isHoatDong();
        topping.setTrangThai(willActive ? "HoatDong" : "TamNgung");
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

    // ==================== TỔNG HỢP & API ====================

    @Override
    @Transactional(readOnly = true)
    public List<ProductOptionDto> getActiveOptionsByType(ProductOptionType type) {
        if (type == ProductOptionType.SIZE) {
            return kichCoRepository.findActiveOrderByGiaPhuThuAsc()
                    .stream()
                    .map(ProductOptionDto::fromKichCo)
                    .toList();
        } else if (type == ProductOptionType.TOPPING) {
            return toppingRepository.findActiveOrderByDonGiaAsc()
                    .stream()
                    .map(ProductOptionDto::fromTopping)
                    .toList();
        }
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOptionStatistics() {
        long totalSizes = kichCoRepository.count();
        long activeSizes = kichCoRepository.findActiveOrderByGiaPhuThuAsc().size();
        long totalToppings = toppingRepository.count();
        long activeToppings = toppingRepository.findActiveOrderByDonGiaAsc().size();

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
