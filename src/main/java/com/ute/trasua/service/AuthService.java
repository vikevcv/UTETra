package com.ute.trasua.service;

import com.ute.trasua.dto.*;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    // public void register(RegisterRequest request) {
    //     if (userRepository.existsByUsername(request.getUsername())) {
    //         throw new RuntimeException("Tên đăng nhập đã tồn tại");
    //     }
    //     if (userRepository.existsByEmail(request.getEmail())) {
    //         throw new RuntimeException("Email đã được sử dụng");
    //     }

    //     User user = User.builder()
    //             .username(request.getUsername())
    //             .password(passwordEncoder.encode(request.getPassword()))
    //             .email(request.getEmail())
    //             .fullName(request.getFullName())
    //             .enabled(true)
    //             .build();
    //     userRepository.save(user);
    // }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Sai tài khoản hoặc mật khẩu"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Sai tài khoản hoặc mật khẩu");
        }

        return tokenProvider.generateToken(user.getUsername());
    }
}