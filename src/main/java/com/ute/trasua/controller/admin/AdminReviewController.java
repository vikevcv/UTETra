package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.review.ProductReviewDto;
import com.ute.trasua.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminReviewController {

    private final ProductReviewService productReviewService;

    @GetMapping
    public String listReviews(Model model) {
        List<ProductReviewDto> reviews = productReviewService.getAllReviews();
        model.addAttribute("reviews", reviews);
        model.addAttribute("siteTitle", "Quản Lý Đánh Giá Phẩm Trà — UTETra Admin");
        return "admin/reviews/list";
    }

    @PostMapping("/{id}/toggle-hide")
    @ResponseBody
    public ResponseEntity<?> toggleHide(
            @PathVariable("id") Long id,
            @RequestParam("isHidden") boolean isHidden) {
        try {
            productReviewService.toggleHideReview(id, isHidden);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", isHidden ? "Đã ẩn đánh giá." : "Đã hiển thị lại đánh giá."
            ));
        } catch (Exception e) {
            log.error("Lỗi ẩn/hiện đánh giá #{}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
