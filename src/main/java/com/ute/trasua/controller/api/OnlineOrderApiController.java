package com.ute.trasua.controller.api;

import com.ute.trasua.dto.order.CheckoutRequest;
import com.ute.trasua.dto.order.CheckoutResponse;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.OnlineOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for creating online delivery orders (Module 3 - UC05).
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OnlineOrderApiController {

    private final OnlineOrderService onlineOrderService;
    private final UserRepository userRepository;

    /**
     * Creates an authoritative online delivery order.
     */
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        User currentUser = resolveCurrentUser();
        CheckoutResponse response = onlineOrderService.createOnlineOrder(request, currentUser);
        return ResponseEntity.ok(response);
    }

    private User resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
