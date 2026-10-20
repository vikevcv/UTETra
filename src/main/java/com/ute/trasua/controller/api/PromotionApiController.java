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
public class PromotionApiController {

    private final PromotionService promotionService;
    private final com.ute.trasua.repository.UserRepository userRepository;

    public PromotionApiController(PromotionService promotionService, com.ute.trasua.repository.UserRepository userRepository) {
        this.promotionService = promotionService;
        this.userRepository = userRepository;
    }

    /**
     * API checking voucher validity and calculating discount in real-time.
     */
    @PostMapping({"/validate", "/apply"})
    public ResponseEntity<VoucherApplyResponse> validateVoucher(@Valid @RequestBody VoucherApplyRequest request) {
        com.ute.trasua.entity.User currentUser = resolveCurrentUser();
        VoucherApplyResponse response = promotionService.validateAndApplyVoucher(
                request.getVoucherCode(), request.getBranchId(), request.getOrderSubtotal(), currentUser
        );
        return ResponseEntity.ok(response);
    }

    private com.ute.trasua.entity.User resolveCurrentUser() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
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
