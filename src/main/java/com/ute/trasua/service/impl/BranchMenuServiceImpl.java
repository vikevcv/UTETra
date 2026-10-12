package com.ute.trasua.service.impl;

import com.ute.trasua.dto.kitchen.BranchMenuStatusDto;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.BranchMenu;
import com.ute.trasua.entity.Product;
import com.ute.trasua.entity.Topping;
import com.ute.trasua.repository.BranchMenuRepository;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.repository.ProductRepository;
import com.ute.trasua.repository.ToppingRepository;
import com.ute.trasua.service.BranchMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Triển khai dịch vụ Cập nhật trạng thái còn/hết hàng chi nhánh (UC11).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BranchMenuServiceImpl implements BranchMenuService {

    private final BranchMenuRepository branchMenuRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final ToppingRepository toppingRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Lưu trữ tồn kho Món Nước theo Chi nhánh trong bộ nhớ: Map<branchId, Map<productId, stock>>
    private static final java.util.concurrent.ConcurrentHashMap<Long, java.util.concurrent.ConcurrentHashMap<Long, Integer>> BRANCH_PRODUCT_STOCKS = new java.util.concurrent.ConcurrentHashMap<>();

    // Lưu trữ tồn kho Topping theo Chi nhánh trong bộ nhớ: Map<branchId, Map<toppingName, stock>>
    private static final java.util.concurrent.ConcurrentHashMap<Long, java.util.concurrent.ConcurrentHashMap<String, Integer>> BRANCH_TOPPING_STOCKS = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    @Transactional(readOnly = true)
    public List<BranchMenuStatusDto> getBranchMenuStatus(Long branchId) {
        Long targetBranchId = (branchId != null) ? branchId : 1L;

        // Lấy danh sách ghi nhận trạng thái tạm hết của chi nhánh
        List<BranchMenu> branchMenus = branchMenuRepository.findByBranchId(targetBranchId);
        Map<Long, Boolean> productAvailMap = branchMenus.stream()
                .collect(Collectors.toMap(bm -> bm.getProduct().getId(), BranchMenu::getIsAvailable, (v1, v2) -> v1));

        var prodStocks = BRANCH_PRODUCT_STOCKS.computeIfAbsent(targetBranchId, bId -> new java.util.concurrent.ConcurrentHashMap<>());
        var topStocks = BRANCH_TOPPING_STOCKS.computeIfAbsent(targetBranchId, bId -> new java.util.concurrent.ConcurrentHashMap<>());

        List<BranchMenuStatusDto> list = new ArrayList<>();

        // 1. Toàn bộ Sản Phẩm Món Nước (Drink Products)
        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            boolean isDbAvail = productAvailMap.getOrDefault(p.getId(), true);
            // Lấy tồn kho thực tế đã trừ, mặc định khởi tạo dựa trên ID
            int defaultStock = (15 + (int)(p.getId() % 10) * 3);
            int currentStock = prodStocks.computeIfAbsent(p.getId(), id -> defaultStock);
            boolean isAvail = isDbAvail && (currentStock > 0);

            list.add(BranchMenuStatusDto.builder()
                    .id(p.getId())
                    .itemType("PRODUCT")
                    .code(p.getProductCode() != null ? p.getProductCode() : "SP-" + p.getId())
                    .name(p.getName())
                    .categoryName(p.getCategory() != null ? p.getCategory().getName() : "Trà Đặc Biệt")
                    .price(p.getBasePrice())
                    .imageUrl(p.getImageUrl())
                    .isAvailable(isAvail)
                    .stockEstimate(currentStock)
                    .build());
        }

        // 2. Toàn bộ Topping
        List<Topping> toppings = toppingRepository.findAll();
        for (Topping t : toppings) {
            String topName = (t.getName() != null ? t.getName() : t.getToppingName());
            boolean isDbAvail = "ACTIVE".equalsIgnoreCase(t.getStatus());
            int defaultStock = (25 + (int)(t.getId() % 5) * 5);
            int currentStock = topStocks.computeIfAbsent(topName, name -> defaultStock);
            boolean isAvail = isDbAvail && (currentStock > 0);

            list.add(BranchMenuStatusDto.builder()
                    .id(t.getId())
                    .itemType("TOPPING")
                    .code("TOP-" + t.getId())
                    .name(topName)
                    .categoryName("Topping Thêm")
                    .price(t.getPrice())
                    .imageUrl("")
                    .isAvailable(isAvail)
                    .stockEstimate(currentStock)
                    .build());
        }

        return list;
    }

    @Override
    @Transactional
    public boolean toggleProductAvailability(Long branchId, Long productId, Boolean isAvailable) {
        Long targetBranchId = (branchId != null) ? branchId : 1L;
        Branch branch = branchRepository.findById(targetBranchId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh ID: " + targetBranchId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món nước ID: " + productId));

        BranchMenu bm = branchMenuRepository.findByBranchIdAndProductId(targetBranchId, productId)
                .orElse(null);

        boolean newStatus = (isAvailable != null) ? isAvailable : (bm == null || !bm.getIsAvailable());

        if (bm == null) {
            bm = BranchMenu.builder()
                    .branch(branch)
                    .product(product)
                    .isAvailable(newStatus)
                    .build();
        } else {
            bm.setIsAvailable(newStatus);
        }
        branchMenuRepository.save(bm);

        log.info(">>> Đã cập nhật trạng thái món '{}' tại chi nhánh '{}': {}",
                product.getName(), branch.getName(), newStatus ? "CÒN HÀNG" : "TẠM HẾT");

        // Broadcast realtime qua WebSocket
        try {
            Map<String, Object> payload = Map.of(
                    "type", "PRODUCT_AVAILABILITY_CHANGED",
                    "branchId", targetBranchId,
                    "productId", productId,
                    "isAvailable", newStatus,
                    "message", "Món [" + product.getName() + "] vừa được cập nhật thành: " + (newStatus ? "Còn hàng" : "Tạm hết")
            );
            messagingTemplate.convertAndSend("/topic/kitchen/" + targetBranchId, (Object) payload);
            messagingTemplate.convertAndSend("/topic/menu/" + targetBranchId, (Object) payload);
        } catch (Exception e) {
            log.warn("Không thể gửi WebSocket event cho toggleProductAvailability: {}", e.getMessage());
        }

        return newStatus;
    }

    @Override
    @Transactional
    public boolean toggleToppingAvailability(Long branchId, Long toppingId, Boolean isAvailable) {
        Long targetBranchId = (branchId != null) ? branchId : 1L;
        Topping topping = toppingRepository.findById(toppingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Topping ID: " + toppingId));

        boolean newStatus = (isAvailable != null) ? isAvailable : !"ACTIVE".equalsIgnoreCase(topping.getStatus());
        topping.setStatus(newStatus ? "ACTIVE" : "INACTIVE");
        toppingRepository.save(topping);

        String toppingName = topping.getName() != null ? topping.getName() : topping.getToppingName();
        log.info(">>> Đã cập nhật trạng thái Topping '{}': {}", toppingName, newStatus ? "CÒN HÀNG" : "TẠM HẾT");

        // Broadcast realtime qua WebSocket
        try {
            Map<String, Object> payload = Map.of(
                    "type", "TOPPING_AVAILABILITY_CHANGED",
                    "branchId", targetBranchId,
                    "toppingId", toppingId,
                    "isAvailable", newStatus,
                    "message", "Topping [" + toppingName + "] vừa được cập nhật thành: " + (newStatus ? "Còn hàng" : "Tạm hết")
            );
            messagingTemplate.convertAndSend("/topic/kitchen/" + targetBranchId, (Object) payload);
            messagingTemplate.convertAndSend("/topic/menu/" + targetBranchId, (Object) payload);
        } catch (Exception e) {
            log.warn("Không thể gửi WebSocket event cho toggleToppingAvailability: {}", e.getMessage());
        }

        return newStatus;
    }

    // Lưu trữ tồn kho Ly theo Chi nhánh trong bộ nhớ ram
    private static final java.util.concurrent.ConcurrentHashMap<Long, java.util.concurrent.ConcurrentHashMap<String, Integer>> BRANCH_CUP_STOCKS = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public java.util.Map<String, Integer> getBranchCupStocks(Long branchId) {
        Long targetBranchId = (branchId != null) ? branchId : 1L;
        return BRANCH_CUP_STOCKS.computeIfAbsent(targetBranchId, bId -> {
            java.util.concurrent.ConcurrentHashMap<String, Integer> map = new java.util.concurrent.ConcurrentHashMap<>();
            map.put("M", 80);
            map.put("L", 50);
            return map;
        });
    }

    @Override
    public void deductCupStock(Long branchId, String sizeName, int quantity) {
        if (sizeName == null || quantity <= 0) return;
        Long targetBranchId = (branchId != null) ? branchId : 1L;
        String cleanSize = sizeName.trim().toUpperCase();
        if (!cleanSize.equals("L") && !cleanSize.equals("M")) {
            cleanSize = cleanSize.contains("L") ? "L" : "M";
        }

        java.util.concurrent.ConcurrentHashMap<String, Integer> stocks = BRANCH_CUP_STOCKS.computeIfAbsent(targetBranchId, bId -> {
            java.util.concurrent.ConcurrentHashMap<String, Integer> map = new java.util.concurrent.ConcurrentHashMap<>();
            map.put("M", 80);
            map.put("L", 50);
            return map;
        });

        int current = stocks.getOrDefault(cleanSize, 50);
        int updated = Math.max(0, current - quantity);
        stocks.put(cleanSize, updated);
        log.info(">>> [CUP INVENTORY] Chi nhánh {} đã trừ {} ly Size {}. Tồn kho còn lại: {}", targetBranchId, quantity, cleanSize, updated);

        // Broadcast realtime cập nhật tồn kho Ly
        try {
            Map<String, Object> payload = Map.of(
                    "type", "CUP_STOCK_UPDATED",
                    "branchId", targetBranchId,
                    "size", cleanSize,
                    "remainingStock", updated,
                    "isAvailable", updated > 0
            );
            messagingTemplate.convertAndSend("/topic/kitchen/" + targetBranchId, (Object) payload);
            messagingTemplate.convertAndSend("/topic/menu/" + targetBranchId, (Object) payload);
        } catch (Exception e) {
            log.warn("Không thể gửi WebSocket event cho CUP_STOCK_UPDATED: {}", e.getMessage());
        }
    }

    @Override
    public void deductProductStock(Long branchId, Long productId, int quantity) {
        if (productId == null || quantity <= 0) return;
        Long targetBranchId = (branchId != null) ? branchId : 1L;

        var prodStocks = BRANCH_PRODUCT_STOCKS.computeIfAbsent(targetBranchId, bId -> new java.util.concurrent.ConcurrentHashMap<>());
        int defaultStock = (15 + (int)(productId % 10) * 3);
        int current = prodStocks.computeIfAbsent(productId, id -> defaultStock);
        int updated = Math.max(0, current - quantity);
        prodStocks.put(productId, updated);

        log.info(">>> [PRODUCT INVENTORY] Chi nhánh {} đã trừ {} ly sản phẩm ID {}. Tồn kho còn: {}",
                targetBranchId, quantity, productId, updated);

        // Nếu về 0, tự động đánh dấu tạm hết hàng
        if (updated == 0) {
            toggleProductAvailability(targetBranchId, productId, false);
        }

        try {
            Map<String, Object> payload = Map.of(
                    "type", "PRODUCT_STOCK_UPDATED",
                    "branchId", targetBranchId,
                    "productId", productId,
                    "remainingStock", updated,
                    "isAvailable", updated > 0
            );
            messagingTemplate.convertAndSend("/topic/kitchen/" + targetBranchId, (Object) payload);
            messagingTemplate.convertAndSend("/topic/menu/" + targetBranchId, (Object) payload);
        } catch (Exception e) {
            log.warn("Không thể gửi WebSocket event PRODUCT_STOCK_UPDATED: {}", e.getMessage());
        }
    }

    @Override
    public void deductToppingStock(Long branchId, String toppingName, int quantity) {
        if (toppingName == null || toppingName.isBlank() || quantity <= 0) return;
        Long targetBranchId = (branchId != null) ? branchId : 1L;
        String cleanTopName = toppingName.trim();

        var topStocks = BRANCH_TOPPING_STOCKS.computeIfAbsent(targetBranchId, bId -> new java.util.concurrent.ConcurrentHashMap<>());
        int defaultStock = 25;
        int current = topStocks.computeIfAbsent(cleanTopName, name -> defaultStock);
        int updated = Math.max(0, current - quantity);
        topStocks.put(cleanTopName, updated);

        log.info(">>> [TOPPING INVENTORY] Chi nhánh {} đã trừ {} phần Topping '{}'. Tồn kho còn: {}",
                targetBranchId, quantity, cleanTopName, updated);

        try {
            Map<String, Object> payload = Map.of(
                    "type", "TOPPING_STOCK_UPDATED",
                    "branchId", targetBranchId,
                    "toppingName", cleanTopName,
                    "remainingStock", updated,
                    "isAvailable", updated > 0
            );
            messagingTemplate.convertAndSend("/topic/kitchen/" + targetBranchId, (Object) payload);
            messagingTemplate.convertAndSend("/topic/menu/" + targetBranchId, (Object) payload);
        } catch (Exception e) {
            log.warn("Không thể gửi WebSocket event TOPPING_STOCK_UPDATED: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProductAvailable(Long branchId, Long productId) {
        if (branchId == null || productId == null) return true;
        return branchMenuRepository.findByBranchIdAndProductId(branchId, productId)
                .map(BranchMenu::getIsAvailable)
                .orElse(true);
    }
}
