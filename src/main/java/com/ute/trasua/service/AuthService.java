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

    private final EmailService emailService;

    // Cache lưu trữ tạm thông tin đăng ký và OTP (key: email, value: OtpData)
    private final java.util.concurrent.ConcurrentHashMap<String, OtpData> otpCache = new java.util.concurrent.ConcurrentHashMap<>();

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class OtpData {
        private String otp;
        private RegisterRequest request;
        private long expiryTime;
    }

    public void initiateRegistration(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        if (!request.isPasswordMatch()) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        // Tạo mã OTP 6 số
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        
        // Lưu vào cache (hết hạn sau 5 phút)
        long expiryTime = System.currentTimeMillis() + 5 * 60 * 1000;
        otpCache.put(request.getEmail(), new OtpData(otp, request, expiryTime));

        // Gửi email
        emailService.sendOtpEmail(request.getEmail(), otp);
    }

    public void verifyOtpAndRegister(String email, String otp) {
        OtpData data = otpCache.get(email);
        if (data == null) {
            throw new RuntimeException("Không tìm thấy phiên đăng ký hoặc đã hết hạn");
        }
        if (System.currentTimeMillis() > data.getExpiryTime()) {
            otpCache.remove(email);
            throw new RuntimeException("Mã OTP đã hết hạn");
        }
        if (!data.getOtp().equals(otp)) {
            throw new RuntimeException("Mã OTP không chính xác");
        }

        // OTP hợp lệ -> Lưu user vào DB
        RegisterRequest request = data.getRequest();
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .enabled(true)
                .build();
        
        userRepository.save(user);
        
        // Xóa khỏi cache sau khi hoàn tất
        otpCache.remove(email);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Sai tài khoản hoặc mật khẩu"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Sai tài khoản hoặc mật khẩu");
        }

        return tokenProvider.generateToken(user.getUsername());
    }
}