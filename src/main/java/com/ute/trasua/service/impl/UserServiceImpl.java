package com.ute.trasua.service.impl;

import com.ute.trasua.dto.user.UserProfileDto;
import com.ute.trasua.dto.user.UpdateProfileRequest;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.CloudinaryService;
import com.ute.trasua.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public UserServiceImpl(UserRepository userRepository,
                           @Autowired(required = false) CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));
        return mapToProfileDto(user);
    }

    @Override
    public UserProfileDto updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));

        user.setFullName(request.getFullName().trim());
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone().trim());
        }
        User saved = userRepository.save(user);
        log.info(">>> Đã cập nhật hồ sơ cá nhân cho người dùng {}", username);
        return mapToProfileDto(saved);
    }

    @Override
    public String updateAvatar(String username, MultipartFile avatarFile) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));

        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn tập tin hình ảnh để tải lên.");
        }

        if (cloudinaryService == null) {
            log.warn("CloudinaryService chưa được cấu hình, không thể tải ảnh đại diện.");
            throw new IllegalStateException("Hệ thống lưu trữ ảnh chưa sẵn sàng.");
        }

        String avatarUrl = cloudinaryService.uploadImage(avatarFile, "avatars");
        user.setAvatar(avatarUrl);
        userRepository.save(user);
        log.info(">>> Đã cập nhật ảnh đại diện cho người dùng {}: {}", username, avatarUrl);
        return avatarUrl;
    }

    private UserProfileDto mapToProfileDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles())
                .build();
    }
}
