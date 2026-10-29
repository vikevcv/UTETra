package com.ute.trasua.service.impl;

import com.ute.trasua.dto.BranchMenuResponse;
import com.ute.trasua.dto.ProductBranchMatrixRow;
import com.ute.trasua.dto.ProductResponse;
import com.ute.trasua.dto.kitchen.BranchMenuStatusDto;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.BranchInventory;
import com.ute.trasua.entity.BranchMenu;
import com.ute.trasua.entity.BranchTopping;
import com.ute.trasua.entity.Ingredient;
import com.ute.trasua.entity.Product;
import com.ute.trasua.entity.Topping;
import com.ute.trasua.repository.BranchInventoryRepository;
import com.ute.trasua.repository.BranchMenuRepository;
import com.ute.trasua.repository.BranchToppingRepository;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.repository.IngredientRepository;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for BranchMenu management (UC22) and Kitchen / Barista KDS (UC11).
 */
@Service
@Slf4j
public class BranchMenuServiceImpl implements BranchMenuService {

    private final BranchMenuRepository branchMenuRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final ToppingRepository toppingRepository;
    private final BranchToppingRepository branchToppingRepository;
    private final BranchInventoryRepository branchInventoryRepository;
    private final IngredientRepository ingredientRepository;
    private final com.ute.trasua.repository.ProductRecipeRepository productRecipeRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public BranchMenuServiceImpl(
            BranchMenuRepository branchMenuRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository,
            ToppingRepository toppingRepository,
            BranchToppingRepository branchToppingRepository,
            BranchInventoryRepository branchInventoryRepository,
            IngredientRepository ingredientRepository,
            com.ute.trasua.repository.ProductRecipeRepository productRecipeRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.branchMenuRepository = branchMenuRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.toppingRepository = toppingRepository;
        this.branchToppingRepository = branchToppingRepository;
        this.branchInventoryRepository = branchInventoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.productRecipeRepository = productRecipeRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // ==========================================
    // UC22: Branch Menu Management & Matrix
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<BranchMenuResponse> getMenuByBranch(Long branchId) {
        return branchMenuRepository.findByBranchId(branchId).stream()
                .filter(bm -> bm.getProduct() != null && "ACTIVE".equalsIgnoreCase(bm.getProduct().getStatus()))
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
        Product product = optional.map(BranchMenu::getProduct)
                .orElseGet(() -> productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món nước ID: " + productId)));

        if (product == null || !"ACTIVE".equalsIgnoreCase(product.getStatus())) {
            throw new IllegalStateException("Món nước này đã bị Quản trị viên (Admin) tạm ngưng kinh doanh trên toàn hệ thống!");
        }

        if (optional.isPresent()) {
            BranchMenu bm = optional.get();
            bm.setAvailable(available);
            branchMenuRepository.save(bm);
        } else {
            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new IllegalArgumentException("Branch not found with ID: " + branchId));

            BranchMenu bm = BranchMenu.builder()
                    .branch(branch)
                    .product(product)
                    .available(available)
                    .build();
            branchMenuRepository.save(bm);
        }

        log.info(">>> Updated Product ID {} availability at Branch ID {} to: {}", productId, branchId, available);

        try {
            String prodName = product.getName() != null ? product.getName() : "Món";
            Map<String, Object> payload = Map.of(
                    "type", "PRODUCT_AVAILABILITY_CHANGED",
                    "branchId", branchId,
                    "productId", productId,
                    "isAvailable", available,
                    "message", "Món [" + prodName + "] vừa được cập nhật thành: " + (available ? "Còn hàng" : "Tạm hết")
            );
            messagingTemplate.convertAndSend("/topic/kitchen/" + branchId, (Object) payload);
            messagingTemplate.convertAndSend("/topic/menu/" + branchId, (Object) payload);
        } catch (Exception e) {
            log.warn("Không thể gửi WebSocket event cho updateProductAvailability: {}", e.getMessage());
        }

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
        return branchMenuRepository.countActiveAndAvailableByBranchId(branchId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalAtBranch(Long branchId) {
        return branchMenuRepository.countActiveProductsByBranchId(branchId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBranchMenuComplete(Long branchId) {
        if (branchId == null) return true;
        long masterCount = productRepository.count();
        if (masterCount == 0) return true;
        long branchCount = branchMenuRepository.countByBranchId(branchId);
        return branchCount >= masterCount;
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

        List<BranchInventory> inventories = branchInventoryRepository.findByBranchId(targetBranchId);
        Map<Long, BigDecimal> invMap = new HashMap<>();
        for (BranchInventory inv : inventories) {
            if (inv.getIngredient() != null && inv.getIngredient().getId() != null) {
                invMap.put(inv.getIngredient().getId(), inv.getStockQuantity() != null ? inv.getStockQuantity() : BigDecimal.ZERO);
            }
        }

        List<BranchMenuStatusDto> list = new ArrayList<>();

        List<Product> products = productRepository.findByStatusOrderByNameAsc("ACTIVE");
        for (Product p : products) {
            boolean isDbAvail = productAvailMap.getOrDefault(p.getId(), true);
            String prodName = p.getName() != null ? p.getName().toLowerCase() : "";

            int currentStock = 0;
            List<com.ute.trasua.entity.ProductRecipe> recipes = productRecipeRepository.findByProductIdWithIngredient(p.getId());
            if (recipes != null && !recipes.isEmpty()) {
                int minCups = Integer.MAX_VALUE;
                for (com.ute.trasua.entity.ProductRecipe recipe : recipes) {
                    if (recipe.getIngredient() == null) continue;
                    BigDecimal portion = recipe.getQuantity() != null ? recipe.getQuantity() : BigDecimal.ZERO;
                    if (portion.compareTo(BigDecimal.ZERO) <= 0) continue;
                    Long ingId = recipe.getIngredient().getId();
                    BigDecimal ingStock = invMap.getOrDefault(ingId, BigDecimal.ZERO);
                    int cups = ingStock.divide(portion, 0, RoundingMode.FLOOR).intValue();
                    if (cups < minCups) {
                        minCups = cups;
                    }
                }
                currentStock = (minCups != Integer.MAX_VALUE) ? Math.max(0, minCups) : 50;
            } else {
                BigDecimal mainTeaStock = BigDecimal.ZERO;
                boolean matchedKeyword = false;
                for (BranchInventory inv : inventories) {
                    if (inv.getIngredient() == null) continue;
                    String name = inv.getIngredient().getName() != null ? inv.getIngredient().getName().toLowerCase() : "";
                    String code = inv.getIngredient().getCode() != null ? inv.getIngredient().getCode().toUpperCase() : "";

                    if (prodName.contains("ô long") && (code.equals("NL003") || name.contains("ô long"))) {
                        mainTeaStock = inv.getStockQuantity() != null ? inv.getStockQuantity() : BigDecimal.ZERO;
                        matchedKeyword = true;
                        break;
                    } else if ((prodName.contains("lục trà") || prodName.contains("nhài")) && (code.equals("NL002") || name.contains("lục trà"))) {
                        mainTeaStock = inv.getStockQuantity() != null ? inv.getStockQuantity() : BigDecimal.ZERO;
                        matchedKeyword = true;
                        break;
                    } else if ((code.equals("NL001") || name.contains("trà đen")) && mainTeaStock.compareTo(BigDecimal.ZERO) == 0) {
                        mainTeaStock = inv.getStockQuantity() != null ? inv.getStockQuantity() : BigDecimal.ZERO;
                        matchedKeyword = true;
                    }
                }
                if (matchedKeyword && mainTeaStock.compareTo(BigDecimal.ZERO) > 0) {
                    currentStock = mainTeaStock.divide(BigDecimal.valueOf(0.02), 0, RoundingMode.FLOOR).intValue();
                } else {
                    currentStock = isDbAvail ? 50 : 0;
                }
            }

            // Trạng thái còn/hết tuân thủ trực tiếp bảng branch_menus (isDbAvail); nếu chưa nhập kho nguyên liệu thì gán tồn kho mặc định 50 ly
            currentStock = isDbAvail ? (currentStock > 0 ? currentStock : 50) : 0;
            boolean isAvail = isDbAvail;

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
            String cleanTopName = topName.toLowerCase();
            boolean isDbAvail = "ACTIVE".equalsIgnoreCase(t.getStatus())
                    && isToppingAvailableAtBranch(targetBranchId, t.getId());

            // Tính tồn kho ước tính dựa trên nguyên liệu topping trong kho chi nhánh (mỗi phần ~ 0.05 đơn vị)
            BigDecimal toppingIngStock = BigDecimal.ZERO;
            for (BranchInventory inv : inventories) {
                if (inv.getIngredient() == null) continue;
                String name = inv.getIngredient().getName() != null ? inv.getIngredient().getName().toLowerCase() : "";
                String code = inv.getIngredient().getCode() != null ? inv.getIngredient().getCode().toUpperCase() : "";

                if (cleanTopName.contains("trân châu") && (code.equals("NL008") || name.contains("trân châu"))) {
                    toppingIngStock = inv.getStockQuantity() != null ? inv.getStockQuantity() : BigDecimal.ZERO;
                    break;
                } else if (cleanTopName.contains("sen") && (code.equals("NL005") || name.contains("sen"))) {
                    toppingIngStock = inv.getStockQuantity() != null ? inv.getStockQuantity() : BigDecimal.ZERO;
                    break;
                } else if (cleanTopName.contains("sương sáo") && (code.equals("NL006") || name.contains("sương sáo"))) {
                    toppingIngStock = inv.getStockQuantity() != null ? inv.getStockQuantity() : BigDecimal.ZERO;
                    break;
                } else if ((cleanTopName.contains("macchiato") || cleanTopName.contains("kem mây") || cleanTopName.contains("kem"))
                        && (code.equals("NL007") || name.contains("kem"))) {
                    toppingIngStock = inv.getStockQuantity() != null ? inv.getStockQuantity() : BigDecimal.ZERO;
                    break;
                }
            }

            int estimatedPortions = toppingIngStock.divide(BigDecimal.valueOf(0.05), 0, RoundingMode.FLOOR).intValue();
            // Trạng thái topping tuân thủ trực tiếp trạng thái Admin (ACTIVE) và trạng thái chi nhánh (branch_toppings)
            int currentStock = isDbAvail ? (estimatedPortions > 0 ? estimatedPortions : 50) : 0;
            boolean isAvail = isDbAvail;

            String topImg = "https://res.cloudinary.com/hueitcyt/image/upload/v1792511985/utetra/defaults/default-tea-zen.jpg";
            if (cleanTopName.contains("hạt sen") || cleanTopName.contains("sen")) {
                topImg = "https://res.cloudinary.com/hueitcyt/image/upload/v1792511980/utetra/toppings/top-01-hat-sen-hue.jpg";
            } else if (cleanTopName.contains("sương sáo") || cleanTopName.contains("thạch")) {
                topImg = "https://res.cloudinary.com/hueitcyt/image/upload/v1792511982/utetra/toppings/top-02-thach-suong-sao.jpg";
            } else if (cleanTopName.contains("kem mây") || cleanTopName.contains("kem") || cleanTopName.contains("macchiato")) {
                topImg = "https://res.cloudinary.com/hueitcyt/image/upload/v1792511983/utetra/toppings/top-03-kem-may-sua-moc.jpg";
            } else if (cleanTopName.contains("trân châu")) {
                topImg = "https://res.cloudinary.com/hueitcyt/image/upload/v1792512015/utetra/toppings/top-04-tran-chau-hoang-kim.jpg";
            }

            list.add(BranchMenuStatusDto.builder()
                    .id(t.getId())
                    .itemType("TOPPING")
                    .code("TOP-" + t.getId())
                    .name(topName)
                    .categoryName("Topping Thêm")
                    .price(t.getPrice())
                    .imageUrl(topImg)
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

        if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
            throw new IllegalStateException("Món nước này đã bị Quản trị viên (Admin) tạm ngưng kinh doanh trên toàn hệ thống!");
        }

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

        boolean newStatus = (isAvailable != null) ? isAvailable : !isToppingAvailableAtBranch(targetBranchId, toppingId);
        BranchTopping row = branchToppingRepository.findByBranchIdAndToppingId(targetBranchId, toppingId)
                .orElse(BranchTopping.builder().branchId(targetBranchId).toppingId(toppingId).isAvailable(true).build());
        row.setIsAvailable(newStatus);
        branchToppingRepository.save(row);

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
    @Transactional(readOnly = true)
    public Map<String, Integer> getBranchCupStocks(Long branchId) {
        Long targetBranchId = (branchId != null) ? branchId : 1L;
        List<BranchInventory> inventories = branchInventoryRepository.findByBranchId(targetBranchId);

        int cupM = 0;
        int cupL = 0;

        for (BranchInventory inv : inventories) {
            if (inv.getIngredient() == null) continue;
            String code = inv.getIngredient().getCode() != null ? inv.getIngredient().getCode().toUpperCase() : "";
            String name = inv.getIngredient().getName() != null ? inv.getIngredient().getName().toLowerCase() : "";

            BigDecimal qty = inv.getStockQuantity() != null ? inv.getStockQuantity() : BigDecimal.ZERO;
            boolean isCay = "cây".equalsIgnoreCase(inv.getIngredient().getUnit()) || "cay".equalsIgnoreCase(inv.getIngredient().getUnit());
            int calculatedCups = isCay ? qty.multiply(BigDecimal.valueOf(50)).intValue() : qty.intValue();

            if (code.equals("NL010") || name.contains("500ml") || name.contains("size m")) {
                cupM = Math.max(0, calculatedCups);
            } else if (code.equals("NL011") || name.contains("700ml") || name.contains("size l")) {
                cupL = Math.max(0, calculatedCups);
            }
        }

        if (cupM == 0 && cupL == 0) {
            cupM = 50;
            cupL = 30;
        }

        Map<String, Integer> map = new HashMap<>();
        map.put("M", cupM);
        map.put("L", cupL);
        return map;
    }

    @Override
    @Transactional
    public void deductCupStock(Long branchId, String sizeName, int quantity) {
        if (sizeName == null || quantity <= 0) return;
        Long targetBranchId = (branchId != null) ? branchId : 1L;
        String cleanSize = sizeName.trim().toUpperCase();
        if (!cleanSize.equals("L") && !cleanSize.equals("M")) {
            cleanSize = cleanSize.contains("L") ? "L" : "M";
        }

        List<BranchInventory> inventories = branchInventoryRepository.findByBranchId(targetBranchId);
        BranchInventory targetInv = null;

        for (BranchInventory inv : inventories) {
            if (inv.getIngredient() == null) continue;
            String code = inv.getIngredient().getCode() != null ? inv.getIngredient().getCode().toUpperCase() : "";
            String name = inv.getIngredient().getName() != null ? inv.getIngredient().getName().toLowerCase() : "";

            if (cleanSize.equals("M") && (code.equals("NL010") || name.contains("500ml") || name.contains("size m"))) {
                targetInv = inv;
                break;
            } else if (cleanSize.equals("L") && (code.equals("NL011") || name.contains("700ml") || name.contains("size l"))) {
                targetInv = inv;
                break;
            }
        }

        int remainingCups = 0;
        if (targetInv != null) {
            boolean isCay = "cây".equalsIgnoreCase(targetInv.getIngredient().getUnit()) || "cay".equalsIgnoreCase(targetInv.getIngredient().getUnit());
            BigDecimal deductQty = isCay
                    ? BigDecimal.valueOf(quantity).divide(BigDecimal.valueOf(50), 4, RoundingMode.HALF_UP)
                    : BigDecimal.valueOf(quantity);

            BigDecimal currentStock = targetInv.getStockQuantity() != null ? targetInv.getStockQuantity() : BigDecimal.ZERO;
            BigDecimal updatedStock = currentStock.subtract(deductQty);
            if (updatedStock.compareTo(BigDecimal.ZERO) < 0) {
                updatedStock = BigDecimal.ZERO;
            }

            targetInv.setStockQuantity(updatedStock);
            targetInv.setUpdatedAt(LocalDateTime.now());
            branchInventoryRepository.save(targetInv);

            remainingCups = isCay ? updatedStock.multiply(BigDecimal.valueOf(50)).intValue() : updatedStock.intValue();
            log.info(">>> [DB INVENTORY] Chi nhánh {} đã trừ {} ly Size {}. Tồn kho CSDL còn lại: {} ly ({} {})",
                    targetBranchId, quantity, cleanSize, remainingCups, updatedStock, targetInv.getIngredient().getUnit());
        } else {
            log.warn("Không tìm thấy bản ghi tồn kho Ly Size {} cho chi nhánh {}", cleanSize, targetBranchId);
        }

        try {
            Map<String, Object> payload = Map.of(
                    "type", "CUP_STOCK_UPDATED",
                    "branchId", targetBranchId,
                    "size", cleanSize,
                    "remainingStock", remainingCups,
                    "isAvailable", remainingCups > 0
            );
            messagingTemplate.convertAndSend("/topic/kitchen/" + targetBranchId, (Object) payload);
            messagingTemplate.convertAndSend("/topic/menu/" + targetBranchId, (Object) payload);
        } catch (Exception e) {
            log.warn("Không thể gửi WebSocket event cho CUP_STOCK_UPDATED: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deductProductStock(Long branchId, Long productId, int quantity) {
        if (productId == null || quantity <= 0) return;
        Long targetBranchId = (branchId != null) ? branchId : 1L;

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return;

        boolean outOfStock = false;

        // ƯU TIÊN 1: Khấu trừ theo công thức pha chế chính thức của món nước (ProductRecipe)
        List<com.ute.trasua.entity.ProductRecipe> recipes = productRecipeRepository.findByProductIdWithIngredient(productId);
        if (recipes != null && !recipes.isEmpty()) {
            for (com.ute.trasua.entity.ProductRecipe recipe : recipes) {
                Ingredient ing = recipe.getIngredient();
                if (ing == null) continue;

                BigDecimal portionPerCup = recipe.getQuantity() != null ? recipe.getQuantity() : BigDecimal.ZERO;
                if (portionPerCup.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal totalDeduct = portionPerCup.multiply(BigDecimal.valueOf(quantity));

                // Lấy kho nguyên liệu chi nhánh (có pessimistic write lock để chống race condition)
                BranchInventory inv = branchInventoryRepository.findByBranchIdAndIngredientIdWithLock(targetBranchId, ing.getId())
                        .orElse(null);

                if (inv != null) {
                    BigDecimal current = inv.getStockQuantity() != null ? inv.getStockQuantity() : BigDecimal.ZERO;
                    BigDecimal updated = current.subtract(totalDeduct);
                    if (updated.compareTo(BigDecimal.ZERO) <= 0) {
                        updated = BigDecimal.ZERO;
                        outOfStock = true;
                    }
                    inv.setStockQuantity(updated);
                    inv.setUpdatedAt(LocalDateTime.now());
                    branchInventoryRepository.save(inv);

                    log.info(">>> [RECIPE DEDUCT] Chi nhánh {} đã trừ {} {} '{}' theo công thức món '{}'. Ước lượng tồn kho còn: {}",
                            targetBranchId, totalDeduct, ing.getUnit(), ing.getName(), product.getName(), updated);
                } else {
                    log.warn(">>> [RECIPE DEDUCT] Chi nhánh {} chưa có tồn kho nguyên liệu '{}' (ID: {}), bỏ qua trừ kho.",
                            targetBranchId, ing.getName(), ing.getId());
                }
            }
        } else {
            // ƯU TIÊN 2: Fallback logic cơ bản khi món chưa được cấu hình công thức
            List<BranchInventory> inventories = branchInventoryRepository.findByBranchId(targetBranchId);
            String prodName = product.getName() != null ? product.getName().toLowerCase() : "";
            String catName = product.getCategory() != null ? product.getCategory().getName().toLowerCase() : "";

            BranchInventory teaInv = null;
            BranchInventory milkInv = null;

            for (BranchInventory inv : inventories) {
                if (inv.getIngredient() == null) continue;
                String code = inv.getIngredient().getCode() != null ? inv.getIngredient().getCode().toUpperCase() : "";
                String name = inv.getIngredient().getName() != null ? inv.getIngredient().getName().toLowerCase() : "";

                if (prodName.contains("ô long") && (code.equals("NL003") || name.contains("ô long"))) {
                    teaInv = inv;
                } else if ((prodName.contains("lục trà") || prodName.contains("nhài")) && (code.equals("NL002") || name.contains("lục trà"))) {
                    teaInv = inv;
                } else if ((code.equals("NL001") || name.contains("trà đen")) && teaInv == null) {
                    teaInv = inv;
                }

                if (catName.contains("trà sữa") && (code.equals("NL004") || name.contains("sữa tươi"))) {
                    milkInv = inv;
                }
            }

            // Trừ trà: 0.02 Kg / ly
            if (teaInv != null) {
                BigDecimal teaDeduct = BigDecimal.valueOf(0.02).multiply(BigDecimal.valueOf(quantity));
                BigDecimal current = teaInv.getStockQuantity() != null ? teaInv.getStockQuantity() : BigDecimal.ZERO;
                BigDecimal updated = current.subtract(teaDeduct);
                if (updated.compareTo(BigDecimal.ZERO) <= 0) {
                    updated = BigDecimal.ZERO;
                    outOfStock = true;
                }
                teaInv.setStockQuantity(updated);
                teaInv.setUpdatedAt(LocalDateTime.now());
                branchInventoryRepository.save(teaInv);
                log.info(">>> [DB INVENTORY FALLBACK] Chi nhánh {} đã trừ {} Kg trà '{}'. Tồn CSDL còn: {}", targetBranchId, teaDeduct, teaInv.getIngredient().getName(), updated);
            }

            // Trừ sữa tươi: 0.10 Lít / ly
            if (milkInv != null) {
                BigDecimal milkDeduct = BigDecimal.valueOf(0.10).multiply(BigDecimal.valueOf(quantity));
                BigDecimal current = milkInv.getStockQuantity() != null ? milkInv.getStockQuantity() : BigDecimal.ZERO;
                BigDecimal updated = current.subtract(milkDeduct);
                if (updated.compareTo(BigDecimal.ZERO) <= 0) {
                    updated = BigDecimal.ZERO;
                    outOfStock = true;
                }
                milkInv.setStockQuantity(updated);
                milkInv.setUpdatedAt(LocalDateTime.now());
                branchInventoryRepository.save(milkInv);
                log.info(">>> [DB INVENTORY FALLBACK] Chi nhánh {} đã trừ {} Lít sữa tươi. Tồn CSDL còn: {}", targetBranchId, milkDeduct, updated);
            }
        }

        // Tự động tắt món khi nguyên liệu hết
        if (outOfStock) {
            toggleProductAvailability(targetBranchId, productId, false);
            log.warn(">>> [AUTO OUT-OF-STOCK] Đã tự động TẮT BÁN món '{}' tại chi nhánh {} do nguyên liệu đã cạn kiệt!", product.getName(), targetBranchId);
        }

        try {
            Map<String, Object> payload = Map.of(
                    "type", "PRODUCT_STOCK_UPDATED",
                    "branchId", targetBranchId,
                    "productId", productId,
                    "isAvailable", !outOfStock
            );
            messagingTemplate.convertAndSend("/topic/kitchen/" + targetBranchId, (Object) payload);
            messagingTemplate.convertAndSend("/topic/menu/" + targetBranchId, (Object) payload);
        } catch (Exception e) {
            log.warn("Không thể gửi WebSocket event PRODUCT_STOCK_UPDATED: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deductToppingStock(Long branchId, String toppingName, int quantity) {
        if (toppingName == null || toppingName.isBlank() || quantity <= 0) return;
        Long targetBranchId = (branchId != null) ? branchId : 1L;
        String cleanTopName = toppingName.trim().toLowerCase();

        List<BranchInventory> inventories = branchInventoryRepository.findByBranchId(targetBranchId);
        BranchInventory targetInv = null;

        for (BranchInventory inv : inventories) {
            if (inv.getIngredient() == null) continue;
            String name = inv.getIngredient().getName() != null ? inv.getIngredient().getName().toLowerCase() : "";
            String code = inv.getIngredient().getCode() != null ? inv.getIngredient().getCode().toUpperCase() : "";

            if (cleanTopName.contains("trân châu") && (code.equals("NL008") || name.contains("trân châu"))) {
                targetInv = inv;
                break;
            } else if (cleanTopName.contains("sen") && (code.equals("NL005") || name.contains("sen"))) {
                targetInv = inv;
                break;
            } else if (cleanTopName.contains("sương sáo") && (code.equals("NL006") || name.contains("sương sáo"))) {
                targetInv = inv;
                break;
            } else if ((cleanTopName.contains("macchiato") || cleanTopName.contains("kem mây") || cleanTopName.contains("kem"))
                    && (code.equals("NL007") || name.contains("kem"))) {
                targetInv = inv;
                break;
            }
        }

        boolean outOfStock = false;
        if (targetInv != null) {
            BigDecimal deductQty = BigDecimal.valueOf(0.05).multiply(BigDecimal.valueOf(quantity));
            BigDecimal current = targetInv.getStockQuantity() != null ? targetInv.getStockQuantity() : BigDecimal.ZERO;
            BigDecimal updated = current.subtract(deductQty);
            if (updated.compareTo(BigDecimal.ZERO) <= 0) {
                updated = BigDecimal.ZERO;
                outOfStock = true;
            }
            targetInv.setStockQuantity(updated);
            targetInv.setUpdatedAt(LocalDateTime.now());
            branchInventoryRepository.save(targetInv);

            log.info(">>> [DB INVENTORY] Chi nhánh {} đã trừ {} phần topping '{}' ({} {}). Tồn CSDL còn: {}",
                    targetBranchId, quantity, toppingName, deductQty, targetInv.getIngredient().getUnit(), updated);
        }

        try {
            Map<String, Object> payload = Map.of(
                    "type", "TOPPING_STOCK_UPDATED",
                    "branchId", targetBranchId,
                    "toppingName", toppingName,
                    "isAvailable", !outOfStock
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
        if (branchId == null || productId == null) return false;
        boolean dbAvail = branchMenuRepository.findByBranchIdAndProductId(branchId, productId)
                .map(BranchMenu::isAvailable)
                .orElse(true);
        if (!dbAvail) {
            return false;
        }

        // Tái sử dụng nguồn sự thật từ getBranchMenuStatus để đảm bảo nhất quán 100% giữa POS UI và backend order gate
        List<BranchMenuStatusDto> statuses = getBranchMenuStatus(branchId);
        return statuses.stream()
                .filter(dto -> "PRODUCT".equalsIgnoreCase(dto.getItemType()) && productId.equals(dto.getId()))
                .findFirst()
                .map(dto -> Boolean.TRUE.equals(dto.getIsAvailable()) && dto.getStockEstimate() != null && dto.getStockEstimate() > 0)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isToppingAvailableAtBranch(Long branchId, Long toppingId) {
        if (branchId == null || toppingId == null) return false;
        return branchToppingRepository.findByBranchIdAndToppingId(branchId, toppingId)
                .map(BranchTopping::getIsAvailable)
                .orElse(true);
    }
}
