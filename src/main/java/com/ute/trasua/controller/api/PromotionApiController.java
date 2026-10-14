package com.ute.trasua.controller.api;

import com.ute.trasua.dto.PromotionResponse;
import com.ute.trasua.dto.VoucherApplyRequest;
import com.ute.trasua.dto.VoucherApplyResponse;
import com.ute.trasua.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionApiController {

    private final PromotionService promotionService;

    /**
     * API checking voucher validity and calculating discount in real-time.
     */
    @PostMapping("/validate")
    public ResponseEntity<VoucherApplyResponse> validateVoucher(@Valid @RequestBody VoucherApplyRequest request) {
        VoucherApplyResponse response = promotionService.validateAndApplyVoucher(
                request.getVoucherCode(), request.getBranchId(), request.getOrderSubtotal()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * API retrieving active available promotions for customer checkout / POS counter.
     */
    @GetMapping("/available")
    public ResponseEntity<List<PromotionResponse>> getAvailablePromotions(
            @RequestParam(value = "branchId", required = false) Long branchId
    ) {
        List<PromotionResponse> promotions = promotionService.getAvailablePromotionsForStore(branchId);
        return ResponseEntity.ok(promotions);
    }
}
