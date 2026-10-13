package com.ute.trasua.service.impl;

import com.ute.trasua.dto.BranchMenuResponse;
import com.ute.trasua.dto.ProductBranchMatrixRow;
import com.ute.trasua.dto.ProductResponse;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.BranchMenu;
import com.ute.trasua.entity.Product;
import com.ute.trasua.repository.BranchMenuRepository;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.repository.ProductRepository;
import com.ute.trasua.service.BranchMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for BranchMenu management (UC22).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BranchMenuServiceImpl implements BranchMenuService {

    private final BranchMenuRepository branchMenuRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

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

        // Lọc theo keyword nếu có
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKw = keyword.trim().toLowerCase();
            products = products.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(lowerKw)) ||
                                 (p.getProductCode() != null && p.getProductCode().toLowerCase().contains(lowerKw)))
                    .collect(Collectors.toList());
        }

        // Lọc theo categoryId nếu có
        if (categoryId != null) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && categoryId.equals(p.getCategory().getId()))
                    .collect(Collectors.toList());
        }

        // Sắp xếp theo thứ tự hiển thị danh mục và tên món
        products = products.stream()
                .sorted(Comparator.comparing((Product p) -> (p.getCategory() != null && p.getCategory().getDisplayOrder() != null) ? p.getCategory().getDisplayOrder() : 999)
                        .thenComparing(p -> p.getName() != null ? p.getName() : "", String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        List<Branch> activeBranches = branchRepository.findByActiveTrue();
        List<BranchMenu> allBranchMenus = branchMenuRepository.findAll();

        // Map: (productId -> (branchId -> BranchMenu))
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
                    // Mặc định còn hàng nếu chưa có cấu hình riêng
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
}
