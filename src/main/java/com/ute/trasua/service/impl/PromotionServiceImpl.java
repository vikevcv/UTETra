package com.ute.trasua.service.impl;

import com.ute.trasua.dto.*;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.Promotion;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.repository.PromotionRepository;
import com.ute.trasua.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final BranchRepository branchRepository;

    public PromotionServiceImpl(PromotionRepository promotionRepository, BranchRepository branchRepository) {
        this.promotionRepository = promotionRepository;
        this.branchRepository = branchRepository;
    }

    // ==================== UC24: ADMIN TOÀN CHUỖI ====================

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionResponse> searchPromotionsForAdmin(String keyword, String scope, Long branchId, String status, Pageable pageable) {
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String cleanScope = (scope != null && !scope.isBlank()) ? scope.trim().toUpperCase() : null;
        String cleanStatus = (status != null && !status.isBlank()) ? status.trim().toUpperCase() : null;

        return promotionRepository.searchForAdmin(cleanKeyword, cleanScope, branchId, cleanStatus, pageable)
                .map(PromotionResponse::fromEntity);
    }

    // ==================== UC18: CHI NHÁNH CỤ THỂ ====================

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionResponse> searchPromotionsForBranch(Long branchId, String keyword, String status, Pageable pageable) {
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String cleanStatus = (status != null && !status.isBlank()) ? status.trim().toUpperCase() : null;

        return promotionRepository.searchForBranch(branchId, cleanKeyword, cleanStatus, pageable)
                .map(PromotionResponse::fromEntity);
    }

    // ==================== CRUD CHUNG ====================

    @Override
    @Transactional(readOnly = true)
    public PromotionResponse getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương trình khuyến mãi với ID: " + id));
        return PromotionResponse.fromEntity(promotion);
    }

    @Override
    @Transactional
    public PromotionResponse createPromotion(CreatePromotionRequest request, Long authenticatedBranchId, boolean isAdmin) {
        String cleanCode = request.getVoucherCode().trim().toUpperCase();
        if (promotionRepository.existsByVoucherCode(cleanCode)) {
            throw new IllegalArgumentException("Mã voucher '" + cleanCode + "' đã tồn tại trên hệ thống.");
        }

        if (request.getEndDate().isBefore(request.getStartDate()) || request.getEndDate().isEqual(request.getStartDate())) {
            throw new IllegalArgumentException("Thời điểm kết thúc phải sau thời điểm bắt đầu.");
        }

        String discountType = "PERCENT".equalsIgnoreCase(request.getDiscountType()) ? "PERCENT" : "FIXED_AMOUNT";
        if ("PERCENT".equals(discountType)) {
            if (request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0 || request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("Tỷ lệ giảm giá phần trăm phải từ 1% đến 100%.");
            }
        } else {
            if (request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Số tiền giảm giá cố định phải lớn hơn 0đ.");
            }
        }

        String scope;
        Branch branch = null;

        if (isAdmin) {
            if ("BRANCH".equalsIgnoreCase(request.getScope()) && request.getBranchId() != null) {
                scope = "BRANCH";
                branch = branchRepository.findById(request.getBranchId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh với ID: " + request.getBranchId()));
            } else {
                scope = "GLOBAL";
            }
        } else {
            // Manager chi nhánh (UC18) chỉ được tạo voucher cho chi nhánh phụ trách
            scope = "BRANCH";
            if (authenticatedBranchId == null) {
                throw new IllegalArgumentException("Không xác định được chi nhánh quản lý.");
            }
            branch = branchRepository.findById(authenticatedBranchId)
                    .orElseThrow(() -> new IllegalArgumentException("Chi nhánh liên kết không tồn tại."));
        }

        Promotion promotion = Promotion.builder()
                .voucherCode(cleanCode)
                .title(request.getTitle().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .discountType(discountType)
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount("PERCENT".equals(discountType) ? request.getMaxDiscountAmount() : null)
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalUsageLimit(request.getTotalUsageLimit())
                .usedCount(0)
                .usageLimitPerUser(request.getUsageLimitPerUser() != null ? request.getUsageLimitPerUser() : 1)
                .scope(scope)
                .branch(branch)
                .status("ACTIVE")
                .build();

        promotion = promotionRepository.save(promotion);
        log.info(">>> Đã tạo voucher mới: [{}] - '{}', Scope: {}, Chi nhánh: {}",
                promotion.getVoucherCode(), promotion.getTitle(), promotion.getScope(),
                promotion.getBranch() != null ? promotion.getBranch().getName() : "Toàn Chuỗi");

        return PromotionResponse.fromEntity(promotion);
    }

    @Override
    @Transactional
    public PromotionResponse updatePromotion(Long id, UpdatePromotionRequest request, Long authenticatedBranchId, boolean isAdmin) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương trình khuyến mãi với ID: " + id));

        // Kiểm tra quyền đối với Manager chi nhánh (UC18)
        if (!isAdmin) {
            if (!"BRANCH".equalsIgnoreCase(promotion.getScope()) ||
                    promotion.getBranch() == null ||
                    !promotion.getBranch().getId().equals(authenticatedBranchId)) {
                throw new IllegalArgumentException("Bạn không có quyền chỉnh sửa chương trình khuyến mãi này.");
            }
        }

        if (request.getEndDate().isBefore(request.getStartDate()) || request.getEndDate().isEqual(request.getStartDate())) {
            throw new IllegalArgumentException("Thời điểm kết thúc phải sau thời điểm bắt đầu.");
        }

        String discountType = "PERCENT".equalsIgnoreCase(request.getDiscountType()) ? "PERCENT" : "FIXED_AMOUNT";
        if ("PERCENT".equals(discountType)) {
            if (request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0 || request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("Tỷ lệ giảm giá phần trăm phải từ 1% đến 100%.");
            }
        } else {
            if (request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Số tiền giảm giá cố định phải lớn hơn 0đ.");
            }
        }

        if (request.getTotalUsageLimit() < promotion.getUsedCount()) {
            throw new IllegalArgumentException("Tổng lượt dùng không thể nhỏ hơn số lượt đã sử dụng (" + promotion.getUsedCount() + " lượt).");
        }

        promotion.setTitle(request.getTitle().trim());
        promotion.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        promotion.setDiscountType(discountType);
        promotion.setDiscountValue(request.getDiscountValue());
        promotion.setMaxDiscountAmount("PERCENT".equals(discountType) ? request.getMaxDiscountAmount() : null);
        promotion.setMinOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO);
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setTotalUsageLimit(request.getTotalUsageLimit());
        promotion.setUsageLimitPerUser(request.getUsageLimitPerUser() != null ? request.getUsageLimitPerUser() : 1);
        if (request.getStatus() != null) {
            promotion.setStatus(request.getStatus().trim().toUpperCase());
        }

        promotion = promotionRepository.save(promotion);
        log.info(">>> Đã cập nhật voucher [ID: {} - {}]", promotion.getId(), promotion.getVoucherCode());
        return PromotionResponse.fromEntity(promotion);
    }

    @Override
    @Transactional
    public boolean togglePromotionStatus(Long id, Long authenticatedBranchId, boolean isAdmin) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương trình khuyến mãi với ID: " + id));

        if (!isAdmin) {
            if (!"BRANCH".equalsIgnoreCase(promotion.getScope()) ||
                    promotion.getBranch() == null ||
                    !promotion.getBranch().getId().equals(authenticatedBranchId)) {
                throw new IllegalArgumentException("Bạn không có quyền thay đổi trạng thái khuyến mãi này.");
            }
        }

        boolean willActive = !"ACTIVE".equalsIgnoreCase(promotion.getStatus());
        promotion.setStatus(willActive ? "ACTIVE" : "INACTIVE");
        promotionRepository.save(promotion);
        log.info(">>> Đã đổi trạng thái voucher [{}] sang: {}", promotion.getVoucherCode(), promotion.getStatus());
        return willActive;
    }

    @Override
    @Transactional
    public void deletePromotion(Long id, Long authenticatedBranchId, boolean isAdmin) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương trình khuyến mãi với ID: " + id));

        if (!isAdmin) {
            if (!"BRANCH".equalsIgnoreCase(promotion.getScope()) ||
                    promotion.getBranch() == null ||
                    !promotion.getBranch().getId().equals(authenticatedBranchId)) {
                throw new IllegalArgumentException("Bạn không có quyền xóa khuyến mãi này.");
            }
        }

        if (promotion.getUsedCount() != null && promotion.getUsedCount() > 0) {
            throw new IllegalArgumentException("Không thể xóa mã voucher đã có " + promotion.getUsedCount() + " lượt sử dụng. Bạn có thể chọn Tạm ngưng để vô hiệu hóa mã.");
        }

        promotionRepository.deleteById(id);
        log.info(">>> Đã xóa voucher [ID: {} - {}]", id, promotion.getVoucherCode());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPromotionStatistics(Long branchId) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        if (branchId == null) {
            // Thống kê toàn chuỗi (UC24)
            long total = promotionRepository.count();
            long globalCount = promotionRepository.countByScope("GLOBAL");
            long activeCount = promotionRepository.countCurrentlyActive(now);
            long inactiveCount = promotionRepository.countByStatus("INACTIVE");
            long branchCount = total - globalCount;

            stats.put("total", total);
            stats.put("global", globalCount);
            stats.put("globalCount", globalCount);
            stats.put("branch", branchCount);
            stats.put("branchCount", branchCount);
            stats.put("active", activeCount);
            stats.put("activeCount", activeCount);
            stats.put("inactive", inactiveCount);
            stats.put("inactiveCount", inactiveCount);
        } else {
            // Thống kê tại chi nhánh (UC18)
            long total = promotionRepository.countByBranchId(branchId);
            long activeCount = promotionRepository.countCurrentlyActiveByBranch(branchId, now);
            long inactiveCount = total - activeCount;

            stats.put("total", total);
            stats.put("active", activeCount);
            stats.put("activeCount", activeCount);
            stats.put("inactive", inactiveCount);
            stats.put("inactiveCount", inactiveCount);
        }

        return stats;
    }

    // ==================== CHECK VOUCHER CHO KHÁCH & POS ====================

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> getAvailablePromotionsForStore(Long branchId) {
        LocalDateTime now = LocalDateTime.now();
        return promotionRepository.findActivePromotionsForStore(branchId, now)
                .stream()
                .map(PromotionResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherApplyResponse validateAndApplyVoucher(String voucherCode, Long branchId, BigDecimal orderSubtotal) {
        if (voucherCode == null || voucherCode.isBlank()) {
            return VoucherApplyResponse.builder()
                    .valid(false)
                    .message("Vui lòng nhập mã voucher.")
                    .build();
        }

        String cleanCode = voucherCode.trim().toUpperCase();
        Promotion promotion = promotionRepository.findByVoucherCode(cleanCode).orElse(null);

        if (promotion == null) {
            return VoucherApplyResponse.builder()
                    .valid(false)
                    .voucherCode(cleanCode)
                    .message("Mã voucher '" + cleanCode + "' không tồn tại.")
                    .build();
        }

        if (!"ACTIVE".equalsIgnoreCase(promotion.getStatus())) {
            return VoucherApplyResponse.builder()
                    .valid(false)
                    .voucherCode(cleanCode)
                    .message("Mã voucher này hiện đang tạm ngưng áp dụng.")
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(promotion.getStartDate())) {
            return VoucherApplyResponse.builder()
                    .valid(false)
                    .voucherCode(cleanCode)
                    .message("Chương trình khuyến mãi này chưa đến thời gian áp dụng.")
                    .build();
        }

        if (now.isAfter(promotion.getEndDate())) {
            return VoucherApplyResponse.builder()
                    .valid(false)
                    .voucherCode(cleanCode)
                    .message("Mã voucher đã hết hạn sử dụng.")
                    .build();
        }

        if (promotion.getUsedCount() >= promotion.getTotalUsageLimit()) {
            return VoucherApplyResponse.builder()
                    .valid(false)
                    .voucherCode(cleanCode)
                    .message("Mã voucher đã hết lượt sử dụng.")
                    .build();
        }

        // Kiểm tra phạm vi chi nhánh
        if ("BRANCH".equalsIgnoreCase(promotion.getScope())) {
            if (branchId == null || promotion.getBranch() == null || !branchId.equals(promotion.getBranch().getId())) {
                String targetBranchName = promotion.getBranch() != null ? promotion.getBranch().getName() : "chi nhánh khác";
                return VoucherApplyResponse.builder()
                        .valid(false)
                        .voucherCode(cleanCode)
                        .message("Voucher này chỉ áp dụng riêng cho khách đặt tại " + targetBranchName + ".")
                        .build();
            }
        }

        // Kiểm tra giá trị đơn tối thiểu
        if (orderSubtotal == null) orderSubtotal = BigDecimal.ZERO;
        if (orderSubtotal.compareTo(promotion.getMinOrderAmount()) < 0) {
            DecimalFormat df = new DecimalFormat("#,###");
            return VoucherApplyResponse.builder()
                    .valid(false)
                    .voucherCode(cleanCode)
                    .message("Đơn hàng chưa đạt giá trị tối thiểu " + df.format(promotion.getMinOrderAmount()) + "đ để áp dụng mã này.")
                    .build();
        }

        BigDecimal discount = promotion.calculateDiscount(orderSubtotal);
        BigDecimal finalAmount = orderSubtotal.subtract(discount).max(BigDecimal.ZERO);

        return VoucherApplyResponse.builder()
                .valid(true)
                .voucherCode(cleanCode)
                .title(promotion.getTitle())
                .discountAmount(discount)
                .finalAmount(finalAmount)
                .message("Áp dụng mã voucher thành công!")
                .build();
    }
}
