package com.ute.trasua.service;

import com.ute.trasua.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service handling promotions and promotional vouchers chain-wide and branch-specific (Module 4 - UC18 & UC24).
 */
public interface PromotionService {

    // ==================== UC24: ADMIN TOÀN CHUỖI ====================
    Page<PromotionResponse> searchPromotionsForAdmin(String keyword, String scope, Long branchId, String status, Pageable pageable);

    // ==================== UC18: CHI NHÁNH CỤ THỂ ====================
    Page<PromotionResponse> searchPromotionsForBranch(Long branchId, String keyword, String status, Pageable pageable);

    // ==================== CRUD CHUNG ====================
    PromotionResponse getPromotionById(Long id);

    PromotionResponse createPromotion(CreatePromotionRequest request, Long authenticatedBranchId, boolean isAdmin);

    PromotionResponse updatePromotion(Long id, UpdatePromotionRequest request, Long authenticatedBranchId, boolean isAdmin);

    boolean togglePromotionStatus(Long id, Long authenticatedBranchId, boolean isAdmin);

    void deletePromotion(Long id, Long authenticatedBranchId, boolean isAdmin);

    Map<String, Object> getPromotionStatistics(Long branchId);

    // ==================== CHECK VOUCHER CHO KHÁCH & POS ====================
    List<PromotionResponse> getAvailablePromotionsForStore(Long branchId);

    VoucherApplyResponse validateAndApplyVoucher(String voucherCode, Long branchId, BigDecimal orderSubtotal);
}
