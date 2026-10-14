package com.ute.trasua.service.impl;

import com.ute.trasua.dto.BranchMenuResponse;
import com.ute.trasua.dto.ProductBranchMatrixRow;
import com.ute.trasua.dto.ProductResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service implementation for BranchMenu management (UC22) and Kitchen / Barista KDS (UC11).
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
    private static final ConcurrentHashMap<Long, ConcurrentHashMap<Long, Integer>> BRANCH_PRODUCT_STOCKS = new ConcurrentHashMap<>();

    // Lưu trữ tồn kho Topping theo Chi nhánh trong bộ nhớ: Map<branchId, Map<toppingName, stock>>
    private static final ConcurrentHashMap<Long, ConcurrentHashMap<String, Integer>> BRANCH_TOPPING_STOCKS = new ConcurrentHashMap<>();

    // Lưu trữ tồn kho Ly theo Chi nhánh trong bộ nhớ: Map<branchId, Map<sizeName, stock>>
    private static final ConcurrentHashMap<Long, ConcurrentHashMap<String, Integer>> BRANCH_CUP_STOCKS = new ConcurrentHashMap<>();

    // ==========================================
    // UC22: Branch Menu Management & Matrix
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<BranchMenuResponse> getMenuByBranch(Long branchId) {
        return branchMenuRepository.findByBranchId(branchId).stream()
                .map(BranchMenuResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BranchMenuResponse> searchBranchMenu(Long branchId,
                                                     String keyword,
                                                     Long categoryId,
                                                     Boolean available,
                                                     Pageable pageable) {
        Page<BranchMenu> page = branchMenuRepository.searchBranchMenus(branchId, keyword, categoryId, available, pageable);
        return page.map(BranchMenuResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductBranchMatrixRow> getProductBranchMatrix(String keyword, Long categoryId) {
        List<Product> products = productRepository.findAll();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKw = keyword.trim().toLowerCase();
            products = products.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(lowerKw)) ||
                                 (p.getProductCode() != null && p.getProductCode().toLowerCase().contains(lowerKw)))
                    .collect(Collectors.toList());
        }

        if (categoryId != null) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && categoryId.equals(p.getCategory().getId()))
                    .collect(Collectors.toList());
        }

        products = products.stream()
                .sorted(Comparator.comparing((Product p) -> (p.getCategory() != null && p.getCategory().getDisplayOrder() != null) ? p.getCategory().getDisplayOrder() : 999)
                        .thenComparing(p -> p.getName() != null ? p.getName() : "", String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        List<Branch> activeBranches = branchRepository.findByActiveTrue();
        List<BranchMenu> allBranchMenus = branchMenuRepository.findAll();

        Map<Long, Map<Long, BranchMenu>> matrixLookup = new HashMap<>();
        for (BranchMenu bm : allBranchMenus) {
            if (bm.getProduct() != null && bm.getBranch() != null) {
                matrixLookup.computeIfAbsent(bm.getProduct().getId(), k -> new HashMap<>())
                        .put(bm.getBranch().getId(), bm);
            }
        }

        List<ProductBranchMatrixRow> rows = new ArrayList<>();
        for (Product p : products) {
            ProductBranchMatrixRow row = new ProductBranchMatrixRow();
            row.setProduct(ProductResponse.fromEntity(p));

            Map<Long, BranchMenuResponse> branchMap = new HashMap<>();
            Map<Long, BranchMenu> pBranches = matrixLookup.getOrDefault(p.getId(), Collections.emptyMap());

            for (Branch b : activeBranches) {
                BranchMenu bm = pBranches.get(b.getId());
                if (bm != null) {
                    branchMap.put(b.getId(), BranchMenuResponse.fromEntity(bm));
                } else {
                    BranchMenuResponse fallback = BranchMenuResponse.builder()
                            .branchId(b.getId())
                            .branchName(b.getName())
                            .branchCode(b.getBranchCode())
                            .productId(p.getId())
                            .productCode(p.getProductCode())
                            .productName(p.getName())
                            .basePrice(p.getBasePrice())
                            .effectivePrice(p.getBasePrice())
                            .available(true)
                            .build();
                    branchMap.put(b.getId(), fallback);
                }
            }
            row.setBranchMenus(branchMap);
            rows.add(row);
        }

        return rows;
    }

    @Override
    @Transactional
    public boolean updateProductAvailability(Long branchId, Long productId, boolean available) {
        Optional<BranchMenu> optional = branchMenuRepository.findByBranchIdAndProductId(branchId, productId);
        if (optional.isPresent()) {
            BranchMenu bm = optional.get();
            bm.setAvailable(available);
            branchMenuRepository.save(bm);
        } else {
            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new IllegalArgumentException("Branch not found with ID: " + branchId));
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

            BranchMenu bm = BranchMenu.builder()
                    .branch(branch)
                    .product(product)
                    .available(available)
                    .build();
            branchMenuRepository.save(bm);
        }

        log.info(">>> Updated Product ID {} availability at Branch ID {} to: {}", productId, branchId, available);
        return available;
    }

    @Override
    @Transactional
    public boolean updateCustomPrice(Long branchId, Long productId, BigDecimal customPrice) {
        Optional<BranchMenu> optional = branchMenuRepository.findByBranchIdAndProductId(branchId, productId);
        if (optional.isPresent()) {
            BranchMenu bm = optional.get();
            bm.setCustomPrice(customPrice);
            branchMenuRepository.save(bm);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void syncAllProductsToBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with ID: " + branchId));

        List<Product> products = productRepository.findAll();
        int added = 0;
        for (Product p : products) {
            if (!branchMenuRepository.existsByBranchIdAndProductId(branchId, p.getId())) {
                BranchMenu bm = BranchMenu.builder()
                        .branch(branch)
                        .product(p)
                        .available(true)
                        .build();
                branchMenuRepository.save(bm);
                added++;
            }
        }
        log.info(">>> Synced {} products into menu of Branch: [{}] {}", added, branch.getBranchCode(), branch.getName());
    }

    @Override
    @Transactional
    public void syncAllProductsToAllBranches() {
        List<Branch> branches = branchRepository.findByActiveTrue();
        for (Branch b : branches) {
            syncAllProductsToBranch(b.getId());
        }
        log.info(">>> Successfully synced menu across all {} active branches.", branches.size());
    }

    @Override
    @Transactional(readOnly = true)
    public long countAvailableAtBranch(Long branchId) {
        return branchMenuRepository.countByBranchIdAndAvailableTrue(branchId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalAtBranch(Long branchId) {
        return branchMenuRepository.countByBranchId(branchId);
    }

    // ==========================================
    // UC11: Kitchen / Barista KDS Status & Stocks
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<BranchMenuStatusDto> getBranchMenuStatus(Long branchId) {
        Long targetBranchId = (branchId != null) ? branchId : 1L;

        List<BranchMenu> branchMenus = branchMenuRepository.findByBranchId(targetBranchId);
        Map<Long, Boolean> productAvailMap = branchMenus.stream()
                .collect(Collectors.toMap(bm -> bm.getProduct().getId(), BranchMenu::isAvailable, (v1, v2) -> v1));

        var prodStocks = BRANCH_PRODUCT_STOCKS.computeIfAbsent(targetBranchId, bId -> new ConcurrentHashMap<>());
        var topStocks = BRANCH_TOPPING_STOCKS.computeIfAbsent(targetBranchId, bId -> new ConcurrentHashMap<>());

        List<BranchMenuStatusDto> list = new ArrayList<>();

        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            boolean isDbAvail = productAvailMap.getOrDefault(p.getId(), true);
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

        boolean newStatus = (isAvailable != null) ? isAvailable : (bm == null || !bm.isAvailable());

        if (bm == null) {
            bm = BranchMenu.builder()
                    .branch(branch)
                    .product(product)
                    .available(newStatus)
                    .build();
        } else {
            bm.setAvailable(newStatus);
        }
        branchMenuRepository.save(bm);

        log.info(">>> Đã cập nhật trạng thái món '{}' tại chi nhánh '{}': {}",
                product.getName(), branch.getName(), newStatus ? "CÒN HÀNG" : "TẠM HẾT");

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

    @Override
    public Map<String, Integer> getBranchCupStocks(Long branchId) {
        Long targetBranchId = (branchId != null) ? branchId : 1L;
        return BRANCH_CUP_STOCKS.computeIfAbsent(targetBranchId, bId -> {
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
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

        ConcurrentHashMap<String, Integer> stocks = BRANCH_CUP_STOCKS.computeIfAbsent(targetBranchId, bId -> {
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
            map.put("M", 80);
            map.put("L", 50);
            return map;
        });

        int current = stocks.getOrDefault(cleanSize, 50);
        int updated = Math.max(0, current - quantity);
        stocks.put(cleanSize, updated);
        log.info(">>> [CUP INVENTORY] Chi nhánh {} đã trừ {} ly Size {}. Tồn kho còn lại: {}", targetBranchId, quantity, cleanSize, updated);

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

        var prodStocks = BRANCH_PRODUCT_STOCKS.computeIfAbsent(targetBranchId, bId -> new ConcurrentHashMap<>());
        int defaultStock = (15 + (int)(productId % 10) * 3);
        int current = prodStocks.computeIfAbsent(productId, id -> defaultStock);
        int updated = Math.max(0, current - quantity);
        prodStocks.put(productId, updated);

        log.info(">>> [PRODUCT INVENTORY] Chi nhánh {} đã trừ {} ly sản phẩm ID {}. Tồn kho còn: {}",
                targetBranchId, quantity, productId, updated);

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

        var topStocks = BRANCH_TOPPING_STOCKS.computeIfAbsent(targetBranchId, bId -> new ConcurrentHashMap<>());
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
                .map(BranchMenu::isAvailable)
                .orElse(true);
    }
}
