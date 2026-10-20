package com.ute.trasua.controller.api;

import com.ute.trasua.dto.user.UserAddressDto;
import com.ute.trasua.dto.user.UserAddressRequest;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.UserAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API Controller for managing customer delivery address book.
 */
@RestController
@RequestMapping("/api/v1/user/addresses")
@RequiredArgsConstructor
@Slf4j
public class UserAddressApiController {

    private final UserAddressService userAddressService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getMyAddresses() {
        User user = resolveCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        List<UserAddressDto> addresses = userAddressService.getAddressesByUserId(user.getId());
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAddressById(@PathVariable("id") Long id) {
        User user = resolveCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        UserAddressDto dto = userAddressService.getAddressById(id, user.getId());
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<?> createAddress(@Valid @RequestBody UserAddressRequest request) {
        User user = resolveCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        UserAddressDto created = userAddressService.createAddress(user.getId(), request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable("id") Long id,
                                           @Valid @RequestBody UserAddressRequest request) {
        User user = resolveCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        UserAddressDto updated = userAddressService.updateAddress(id, user.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable("id") Long id) {
        User user = resolveCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        userAddressService.deleteAddress(id, user.getId());
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa địa chỉ."));
    }

    @PostMapping("/{id}/default")
    public ResponseEntity<?> setDefaultAddress(@PathVariable("id") Long id) {
        User user = resolveCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        userAddressService.setDefaultAddress(id, user.getId());
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã đặt làm mặc định."));
    }

    private User resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
