package com.ute.trasua.controller.api;

import com.ute.trasua.dto.review.ProductReviewDto;
import com.ute.trasua.dto.review.ReviewSubmitRequest;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReviewApiController {

    private final ProductReviewService productReviewService;
    private final UserRepository userRepository;

    @PostMapping("/api/v1/orders/{orderCode}/reviews")
    public ResponseEntity<?> submitReviews(
            @PathVariable("orderCode") String orderCode,
            @Valid @RequestBody ReviewSubmitRequest request,
            org.springframework.validation.BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", errorMsg != null ? errorMsg : "Dữ liệu đánh giá không hợp lệ"
            ));
        }

        User currentUser = resolveCurrentUser();
        try {
            List<ProductReviewDto> result = productReviewService.submitOrderReviews(orderCode, request, currentUser);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cảm ơn bạn đã đánh giá chất lượng sản phẩm!",
                    "reviews", result
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Lỗi gửi đánh giá đơn {}: {}", orderCode, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra khi lưu đánh giá."
            ));
        }
    }

    @GetMapping("/api/v1/orders/{orderCode}/reviews")
    public ResponseEntity<?> getOrderReviews(@PathVariable("orderCode") String orderCode) {
        List<ProductReviewDto> list = productReviewService.getOrderReviews(orderCode);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "reviews", list
        ));
    }

    @GetMapping("/api/v1/products/{productId}/reviews")
    public ResponseEntity<?> getProductReviews(
            @PathVariable("productId") Long productId,
            @RequestParam(value = "star", required = false) Integer star,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "all", defaultValue = "false") boolean all) {

        Map<String, Object> summary = productReviewService.getProductReviewSummary(productId, star, limit, all);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", summary
        ));
    }

    private User resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
