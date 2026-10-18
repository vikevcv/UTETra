package com.ute.trasua.service.impl;

import com.ute.trasua.dto.BranchInventoryResponse;
import com.ute.trasua.dto.ImportReceiptItemRequest;
import com.ute.trasua.dto.ImportReceiptRequest;
import com.ute.trasua.dto.ImportReceiptResponse;
import com.ute.trasua.entity.*;
import com.ute.trasua.repository.*;
import com.ute.trasua.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final BranchInventoryRepository branchInventoryRepository;
    private final ImportReceiptRepository importReceiptRepository;
    private final ImportReceiptDetailRepository importReceiptDetailRepository;
    private final BranchMenuRepository branchMenuRepository;

    public InventoryServiceImpl(
            BranchRepository branchRepository,
            UserRepository userRepository,
            IngredientRepository ingredientRepository,
            BranchInventoryRepository branchInventoryRepository,
            ImportReceiptRepository importReceiptRepository,
            ImportReceiptDetailRepository importReceiptDetailRepository,
            BranchMenuRepository branchMenuRepository
    ) {
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.ingredientRepository = ingredientRepository;
        this.branchInventoryRepository = branchInventoryRepository;
        this.importReceiptRepository = importReceiptRepository;
        this.importReceiptDetailRepository = importReceiptDetailRepository;
        this.branchMenuRepository = branchMenuRepository;
    }

    @Override
    public List<BranchInventoryResponse> getStockByBranch(Long branchId, String keyword, Boolean isLowStock) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return branchInventoryRepository.searchStockByBranch(branchId, cleanKeyword, isLowStock).stream()
                .map(BranchInventoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BranchInventoryResponse> getStockByBranchPaged(Long branchId, String keyword, Boolean isLowStock, Pageable pageable) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return branchInventoryRepository.searchStockByBranchPaged(branchId, cleanKeyword, isLowStock, pageable)
                .map(BranchInventoryResponse::fromEntity);
    }

    @Override
    @Transactional
    public BranchInventoryResponse updateMinThreshold(Long branchId, Long ingredientId, BigDecimal minThreshold) {
        if (minThreshold == null || minThreshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Ngưỡng tối thiểu không được âm!");
        }

        BranchInventory inventory = branchInventoryRepository.findByBranchIdAndIngredientId(branchId, ingredientId)
                .orElseGet(() -> {
                    Branch branch = branchRepository.findById(branchId)
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh: " + branchId));
                    Ingredient ingredient = ingredientRepository.findById(ingredientId)
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguyên liệu: " + ingredientId));
                    return BranchInventory.builder()
                            .branch(branch)
                            .ingredient(ingredient)
                            .stockQuantity(BigDecimal.ZERO)
                            .minThreshold(minThreshold)
                            .build();
                });

        inventory.setMinThreshold(minThreshold);
        BranchInventory saved = branchInventoryRepository.save(inventory);
        log.info(">>> Đã cập nhật ngưỡng tồn tối thiểu chi nhánh {}: nguyên liệu {} -> {}", branchId, ingredientId, minThreshold);
        return BranchInventoryResponse.fromEntity(saved);
    }

    @Override
    public long countLowStock(Long branchId) {
        return branchInventoryRepository.countLowStockByBranch(branchId);
    }

    @Override
    public long countOutOfStock(Long branchId) {
        return branchInventoryRepository.countOutOfStockByBranch(branchId);
    }

    @Override
    @Transactional
    public ImportReceiptResponse createImportReceipt(Long branchId, String username, ImportReceiptRequest request) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh ID: " + branchId));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Phiếu nhập kho phải có ít nhất 1 mặt hàng nguyên liệu!");
        }

        User user = null;
        if (username != null && !username.trim().isEmpty()) {
            user = userRepository.findByUsername(username.trim()).orElse(null);
        }

        // Sinh mã phiếu nhập chuẩn biểu mẫu BM2: PNK-{BranchCode}-{yyyyMMdd}-{counter}
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String receiptCode = generateReceiptCode(branch.getBranchCode(), dateStr);

        ImportReceipt receipt = ImportReceipt.builder()
                .receiptCode(receiptCode)
                .branch(branch)
                .importDate(LocalDateTime.now())
                .supplierType(request.getSupplierType() != null ? request.getSupplierType() : "CENTRAL_WAREHOUSE")
                .supplierName(request.getSupplierName())
                .deliveredBy(request.getDeliveredBy().trim())
                .receivedBy(request.getReceivedBy().trim())
                .createdBy(user)
                .status("IMPORTED")
                .note(request.getNote())
                .totalAmount(BigDecimal.ZERO)
                .build();

        // Xử lý từng dòng nguyên liệu & cộng dồn tồn kho chi nhánh
        for (ImportReceiptItemRequest itemReq : request.getItems()) {
            Ingredient ingredient = ingredientRepository.findById(itemReq.getIngredientId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguyên liệu ID: " + itemReq.getIngredientId()));

            BigDecimal qty = itemReq.getQuantity();
            BigDecimal price = itemReq.getUnitPrice();
            if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Số lượng nhập của '" + ingredient.getName() + "' phải lớn hơn 0!");
            }
            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Đơn giá nhập của '" + ingredient.getName() + "' không được âm!");
            }

            BigDecimal subtotal = qty.multiply(price);

            ImportReceiptDetail detail = ImportReceiptDetail.builder()
                    .ingredient(ingredient)
                    .quantity(qty)
                    .unitPrice(price)
                    .totalPrice(subtotal)
                    .expiryDate(itemReq.getExpiryDate())
                    .note(itemReq.getNote())
                    .build();

            receipt.addDetail(detail);

            // Cộng dồn tồn kho trong BranchInventory
            BranchInventory inventory = branchInventoryRepository.findByBranchIdAndIngredientId(branchId, ingredient.getId())
                    .orElseGet(() -> BranchInventory.builder()
                            .branch(branch)
                            .ingredient(ingredient)
                            .stockQuantity(BigDecimal.ZERO)
                            .minThreshold(new BigDecimal("10.00"))
                            .build());

            BigDecimal currentStock = inventory.getStockQuantity() != null ? inventory.getStockQuantity() : BigDecimal.ZERO;
            inventory.setStockQuantity(currentStock.add(qty));
            inventory.setUpdatedAt(LocalDateTime.now());
            branchInventoryRepository.save(inventory);
            log.info(">>> Tồn kho chi nhánh {}: nguyên liệu '{}' đã cộng {} {} (Tổng mới: {})",
                    branch.getBranchCode(), ingredient.getName(), qty, ingredient.getUnit(), inventory.getStockQuantity());
        }

        // Tự động mở bán lại các món nước tạm ngưng bán do hết hàng tại chi nhánh (Task 4.2)
        autoReopenBranchMenuIfAvailable(branchId);

        receipt.recalculateTotalAmount();
        ImportReceipt saved = importReceiptRepository.save(receipt);
        log.info(">>> Đã lưu thành công phiếu nhập kho BM2: {} (Tổng tiền: {} VNĐ)", saved.getReceiptCode(), saved.getTotalAmount());

        return ImportReceiptResponse.fromEntity(saved);
    }

    private String generateReceiptCode(String branchCode, String dateStr) {
        String baseCode = String.format("PNK-%s-%s", branchCode, dateStr);
        String finalCode = baseCode + "-" + String.format("%03d", (importReceiptRepository.count() + 1) % 1000);
        if (importReceiptRepository.existsByReceiptCode(finalCode)) {
            finalCode = baseCode + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        }
        return finalCode;
    }

    /**
     * Tự động kích hoạt lại các món nước tạm ngưng khi đã nhập đủ nguyên liệu kho (Task 4.2 / UC29).
     */
    private void autoReopenBranchMenuIfAvailable(Long branchId) {
        try {
            List<BranchMenu> unavailableMenus = branchMenuRepository.findByBranchId(branchId).stream()
                    .filter(bm -> !bm.isAvailable())
                    .collect(Collectors.toList());

            if (!unavailableMenus.isEmpty()) {
                for (BranchMenu bm : unavailableMenus) {
                    bm.setAvailable(true);
                    branchMenuRepository.save(bm);
                    log.info(">>> Đã tự động mở bán lại món '{}' tại chi nhánh ID: {} sau khi nhập kho",
                            bm.getProduct().getName(), branchId);
                }
            }
        } catch (Exception ex) {
            log.warn("Lưu ý: Không thể tự động mở bán BranchMenu cho chi nhánh {}: {}", branchId, ex.getMessage());
        }
    }

    @Override
    public List<ImportReceiptResponse> getReceiptsByBranch(Long branchId, String keyword, String status, LocalDateTime startDate, LocalDateTime endDate) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        return importReceiptRepository.searchReceipts(branchId, cleanKeyword, cleanStatus, startDate, endDate).stream()
                .map(ImportReceiptResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ImportReceiptResponse> getReceiptsByBranchPaged(Long branchId, String keyword, String status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        return importReceiptRepository.searchReceiptsPaged(branchId, cleanKeyword, cleanStatus, startDate, endDate, pageable)
                .map(ImportReceiptResponse::fromEntity);
    }

    @Override
    public ImportReceiptResponse getReceiptById(Long id) {
        ImportReceipt receipt = importReceiptRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập kho có ID: " + id));
        return ImportReceiptResponse.fromEntity(receipt);
    }

    @Override
    public ImportReceiptResponse getReceiptByCode(String receiptCode) {
        ImportReceipt receipt = importReceiptRepository.findByReceiptCodeWithDetails(receiptCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập kho có mã: " + receiptCode));
        return ImportReceiptResponse.fromEntity(receipt);
    }

    @Override
    @Transactional
    public BranchInventory deductIngredientStock(Long branchId, Long ingredientId, BigDecimal quantity) {
        if (branchId == null || ingredientId == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BranchInventory inventory = branchInventoryRepository.findByBranchIdAndIngredientId(branchId, ingredientId)
                .orElse(null);

        if (inventory == null) {
            log.warn("Không tìm thấy tồn kho cho chi nhánh ID: {} và nguyên liệu ID: {}", branchId, ingredientId);
            return null;
        }

        BigDecimal current = inventory.getStockQuantity() != null ? inventory.getStockQuantity() : BigDecimal.ZERO;
        BigDecimal updated = current.subtract(quantity);
        if (updated.compareTo(BigDecimal.ZERO) < 0) {
            updated = BigDecimal.ZERO;
        }

        inventory.setStockQuantity(updated);
        inventory.setUpdatedAt(LocalDateTime.now());
        BranchInventory saved = branchInventoryRepository.save(inventory);

        log.info(">>> [DB INVENTORY] Chi nhánh #{} đã trừ {} {} nguyên liệu '{}'. Tồn kho CSDL còn lại: {}",
                branchId, quantity, saved.getIngredient().getUnit(), saved.getIngredient().getName(), updated);

        // Tự động tắt món khi nguyên liệu hết (stockQuantity <= 0)
        if (updated.compareTo(BigDecimal.ZERO) <= 0) {
            autoDisableBranchMenuWhenOutOfIngredient(branchId, saved.getIngredient());
        }

        return saved;
    }

    /**
     * Tự động tắt bán các món nước phụ thuộc khi nguyên liệu kho đã cạn kiệt (Task 4.2 / UC29).
     */
    private void autoDisableBranchMenuWhenOutOfIngredient(Long branchId, Ingredient ingredient) {
        if (ingredient == null) return;
        try {
            String ingName = ingredient.getName().toLowerCase();
            List<BranchMenu> availableMenus = branchMenuRepository.findByBranchId(branchId).stream()
                    .filter(BranchMenu::isAvailable)
                    .collect(Collectors.toList());

            for (BranchMenu bm : availableMenus) {
                if (bm.getProduct() == null) continue;
                String prodName = bm.getProduct().getName().toLowerCase();
                String catName = bm.getProduct().getCategory() != null ? bm.getProduct().getCategory().getName().toLowerCase() : "";

                boolean shouldDisable = false;
                // Nếu hết Trà Đen -> tắt các món trà đen, trà sữa truyền thống
                if (ingName.contains("trà đen") && (prodName.contains("trà đen") || prodName.contains("truyền thống") || catName.contains("trà sữa"))) {
                    shouldDisable = true;
                }
                // Nếu hết Lục Trà -> tắt các món lục trà, trà nhài
                else if (ingName.contains("lục trà") && (prodName.contains("lục trà") || prodName.contains("nhài") || prodName.contains("hoa nhài"))) {
                    shouldDisable = true;
                }
                // Nếu hết Trà Ô Long -> tắt các món ô long
                else if (ingName.contains("ô long") && prodName.contains("ô long")) {
                    shouldDisable = true;
                }
                // Nếu hết Sữa Tươi -> tắt tất cả các món thuộc danh mục Trà sữa
                else if (ingName.contains("sữa tươi") && catName.contains("trà sữa")) {
                    shouldDisable = true;
                }

                if (shouldDisable) {
                    bm.setAvailable(false);
                    branchMenuRepository.save(bm);
                    log.warn(">>> [AUTO OUT-OF-STOCK] Đã tự động TẮT BÁN món '{}' tại chi nhánh #{} do cạn kiệt nguyên liệu '{}'",
                            bm.getProduct().getName(), branchId, ingredient.getName());
                }
            }
        } catch (Exception ex) {
            log.warn("Lưu ý: Lỗi khi tự động tắt món theo nguyên liệu hết: {}", ex.getMessage());
        }
    }
}
