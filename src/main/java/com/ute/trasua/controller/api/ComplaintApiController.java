package com.ute.trasua.controller.api;

import com.ute.trasua.dto.complaint.ComplaintSubmitRequest;
import com.ute.trasua.dto.complaint.OrderComplaintDto;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.OrderComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ComplaintApiController {

    private final OrderComplaintService orderComplaintService;
    private final UserRepository userRepository;

    @PostMapping("/api/v1/orders/{orderCode}/complaints")
    public ResponseEntity<?> submitComplaint(
            @PathVariable("orderCode") String orderCode,
            @Valid @RequestBody ComplaintSubmitRequest request,
            org.springframework.validation.BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", errorMsg != null ? errorMsg : "Dữ liệu khiếu nại không hợp lệ"
            ));
        }

        User currentUser = resolveCurrentUser();
        try {
            OrderComplaintDto result = orderComplaintService.submitComplaint(orderCode, request, currentUser);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Quán đã tiếp nhận khiếu nại của bạn và sẽ xử lý sớm nhất.",
                    "complaint", result
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Lỗi gửi khiếu nại đơn {}: {}", orderCode, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra khi gửi khiếu nại."
            ));
        }
    }

    @GetMapping("/api/v1/orders/{orderCode}/complaints")
    public ResponseEntity<?> getComplaint(@PathVariable("orderCode") String orderCode) {
        OrderComplaintDto dto = orderComplaintService.getComplaintByOrderCode(orderCode);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "hasComplaint", dto != null,
                "complaint", dto != null ? dto : Map.of()
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
