package com.ute.trasua.service;

import com.ute.trasua.dto.*;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final long OTP_EXPIRATION_MS = 5 * 60 * 1000; // 5 phút
    private static final long RESEND_COOLDOWN_MS = 60 * 1000; // 60 giây
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long RESET_TOKEN_EXPIRATION_MS = 10 * 60 * 1000; // 10 phút

    // Cache lưu trữ tạm thông tin đăng ký (key: email, value: RegisterOtpData)
    private final ConcurrentHashMap<String, RegisterOtpData> registerOtpCache = new ConcurrentHashMap<>();

    // Cache lưu trữ tạm thông tin quên mật khẩu (key: email, value:
    // ForgotPasswordData)
    private final ConcurrentHashMap<String, ForgotPasswordData> forgotPasswordCache = new ConcurrentHashMap<>();

    @Data
    @AllArgsConstructor
    private static class RegisterOtpData {
        private String otp;
        private RegisterRequest request;
        private long expiryTime;
        private int failedAttempts;
        private long lastSentTime;
    }

    @Data
    @AllArgsConstructor
    private static class ForgotPasswordData {
        private String email;
        private String otp;
        private long expiryTime;
        private int failedAttempts;
        private long lastSentTime;
        private String resetToken;
        private long resetTokenExpiry;
    }

    // =========================================================================
    // 1. ĐĂNG KÝ VÀ XÁC THỰC OTP
    // =========================================================================

    public void initiateRegistration(RegisterRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        request.setEmail(cleanEmail);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại trên hệ thống");
        }
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new RuntimeException("Email này đã được sử dụng bởi một tài khoản khác");
        }
        if (!request.isPasswordMatch()) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        // Kiểm tra cooldown gửi lại mã OTP (tối thiểu 60s)
        RegisterOtpData existing = registerOtpCache.get(cleanEmail);
        long now = System.currentTimeMillis();
        if (existing != null && (now - existing.getLastSentTime()) < RESEND_COOLDOWN_MS) {
            long remainingSeconds = (RESEND_COOLDOWN_MS - (now - existing.getLastSentTime())) / 1000;
            throw new RuntimeException("Vui lòng đợi " + remainingSeconds + " giây trước khi yêu cầu gửi lại OTP.");
        }

        // Sinh mã OTP 6 số ngẫu nhiên an toàn mật mã học
        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1000000));

        // Lưu vào cache
        long expiryTime = now + OTP_EXPIRATION_MS;
        registerOtpCache.put(cleanEmail, new RegisterOtpData(otp, request, expiryTime, 0, now));

        // Gửi email xác nhận qua Brevo SMTP Relay
        emailService.sendOtpEmail(cleanEmail, otp);
    }

    public void resendRegisterOtp(String email) {
        String cleanEmail = email.trim().toLowerCase();
        RegisterOtpData data = registerOtpCache.get(cleanEmail);
        if (data == null) {
            throw new RuntimeException("Không tìm thấy phiên đăng ký hoặc mã OTP đã hết hạn. Vui lòng đăng ký lại.");
        }

        long now = System.currentTimeMillis();
        if ((now - data.getLastSentTime()) < RESEND_COOLDOWN_MS) {
            long remainingSeconds = (RESEND_COOLDOWN_MS - (now - data.getLastSentTime())) / 1000;
            throw new RuntimeException("Vui lòng đợi " + remainingSeconds + " giây trước khi yêu cầu gửi lại OTP.");
        }

        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1000000));
        data.setOtp(otp);
        data.setExpiryTime(now + OTP_EXPIRATION_MS);
        data.setFailedAttempts(0);
        data.setLastSentTime(now);

        emailService.sendOtpEmail(cleanEmail, otp);
    }

    public void verifyOtpAndRegister(String email, String otp) {
        String cleanEmail = email.trim().toLowerCase();
        RegisterOtpData data = registerOtpCache.get(cleanEmail);
        if (data == null) {
            throw new RuntimeException("Không tìm thấy phiên đăng ký hoặc mã OTP đã hết hạn.");
        }

        long now = System.currentTimeMillis();
        if (now > data.getExpiryTime()) {
            registerOtpCache.remove(cleanEmail);
            throw new RuntimeException("Mã OTP đã hết hạn. Vui lòng đăng ký lại.");
        }

        if (!data.getOtp().equals(otp)) {
            data.setFailedAttempts(data.getFailedAttempts() + 1);
            int remaining = MAX_FAILED_ATTEMPTS - data.getFailedAttempts();
            if (remaining <= 0) {
                registerOtpCache.remove(cleanEmail);
                throw new RuntimeException(
                        "Bạn đã nhập sai mã OTP quá 3 lần. Phiên đăng ký đã bị hủy để đảm bảo an toàn.");
            }
            throw new RuntimeException("Mã OTP không chính xác, vui lòng thử lại.");
        }

        // OTP hợp lệ -> Lưu user vào MySQL
        RegisterRequest request = data.getRequest();
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(cleanEmail)
                .enabled(true)
                .build();

        userRepository.save(user);

        // Xóa thông tin đăng ký khỏi bộ nhớ cache
        registerOtpCache.remove(cleanEmail);
    }

    // =========================================================================
    // 2. QUÊN MẬT KHẨU & ĐẶT LẠI MẬT KHẨU
    // =========================================================================

    public void initiateForgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        boolean exists = userRepository.existsByEmail(email);

        // Kiểm tra sự tồn tại của email
        if (!exists) {
            throw new RuntimeException("Không tồn tại email.");
        }

        long now = System.currentTimeMillis();
        ForgotPasswordData existing = forgotPasswordCache.get(email);
        if (existing != null && (now - existing.getLastSentTime()) < RESEND_COOLDOWN_MS) {
            long remainingSeconds = (RESEND_COOLDOWN_MS - (now - existing.getLastSentTime())) / 1000;
            throw new RuntimeException("Vui lòng đợi " + remainingSeconds + " giây trước khi yêu cầu mã OTP mới.");
        }

        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1000000));
        long expiryTime = now + OTP_EXPIRATION_MS;

        forgotPasswordCache.put(email, new ForgotPasswordData(
                email, otp, expiryTime, 0, now, null, 0));

        // Gửi email OTP quên mật khẩu
        emailService.sendForgotPasswordOtpEmail(email, otp);
    }

    public void resendForgotPasswordOtp(String email) {
        String cleanEmail = email.trim().toLowerCase();
        boolean exists = userRepository.existsByEmail(cleanEmail);
        if (!exists) {
            return;
        }

        long now = System.currentTimeMillis();
        ForgotPasswordData existing = forgotPasswordCache.get(cleanEmail);
        if (existing != null && (now - existing.getLastSentTime()) < RESEND_COOLDOWN_MS) {
            long remainingSeconds = (RESEND_COOLDOWN_MS - (now - existing.getLastSentTime())) / 1000;
            throw new RuntimeException("Vui lòng đợi " + remainingSeconds + " giây trước khi yêu cầu gửi lại OTP.");
        }

        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1000000));
        long expiryTime = now + OTP_EXPIRATION_MS;

        forgotPasswordCache.put(cleanEmail, new ForgotPasswordData(
                cleanEmail, otp, expiryTime, 0, now, null, 0));

        emailService.sendForgotPasswordOtpEmail(cleanEmail, otp);
    }

    public String verifyForgotPasswordOtp(String email, String otp) {
        String cleanEmail = email.trim().toLowerCase();
        ForgotPasswordData data = forgotPasswordCache.get(cleanEmail);
        if (data == null) {
            throw new RuntimeException("Không tìm thấy yêu cầu đặt lại mật khẩu hoặc mã OTP đã hết hạn.");
        }

        long now = System.currentTimeMillis();
        if (now > data.getExpiryTime()) {
            forgotPasswordCache.remove(cleanEmail);
            throw new RuntimeException("Mã OTP đã hết hạn. Vui lòng thực hiện lại từ đầu.");
        }

        if (!data.getOtp().equals(otp)) {
            data.setFailedAttempts(data.getFailedAttempts() + 1);
            int remaining = MAX_FAILED_ATTEMPTS - data.getFailedAttempts();
            if (remaining <= 0) {
                forgotPasswordCache.remove(cleanEmail);
                throw new RuntimeException("Bạn đã nhập sai mã OTP quá 3 lần. Yêu cầu đã bị hủy.");
            }
            throw new RuntimeException("Mã OTP không chính xác, vui lòng thử lại.");
        }

        // OTP chính xác -> Sinh mã token đặt lại mật khẩu tạm thời
        String resetToken = UUID.randomUUID().toString();
        data.setResetToken(resetToken);
        data.setResetTokenExpiry(now + RESET_TOKEN_EXPIRATION_MS);

        return resetToken;
    }

    public boolean validateResetToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        long now = System.currentTimeMillis();
        for (ForgotPasswordData data : forgotPasswordCache.values()) {
            if (token.equals(data.getResetToken()) && now <= data.getResetTokenExpiry()) {
                return true;
            }
        }
        return false;
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (!request.isPasswordMatch()) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp.");
        }

        long now = System.currentTimeMillis();
        ForgotPasswordData targetData = null;
        String targetEmail = null;

        for (Map.Entry<String, ForgotPasswordData> entry : forgotPasswordCache.entrySet()) {
            ForgotPasswordData data = entry.getValue();
            if (request.getToken().equals(data.getResetToken())) {
                if (now <= data.getResetTokenExpiry()) {
                    targetData = data;
                    targetEmail = entry.getKey();
                }
                break;
            }
        }

        if (targetData == null || targetEmail == null) {
            throw new RuntimeException("Mã xác nhận phiên đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
        }

        User user = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tài khoản người dùng."));

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // Hủy phiên quên mật khẩu sau khi đổi thành công
        forgotPasswordCache.remove(targetEmail);
    }

    // =========================================================================
    // 3. ĐĂNG NHẬP
    // =========================================================================

    public String login(LoginRequest request) {
    try {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        return tokenProvider.generateToken(authentication.getName());
    } catch (BadCredentialsException e) {
        throw new RuntimeException("Sai tài khoản hoặc mật khẩu");
    } catch (DisabledException e) {
        throw new RuntimeException("Tài khoản của bạn đã bị vô hiệu hóa hoặc chưa kích hoạt");
    } catch (org.springframework.security.authentication.LockedException e) {
        throw new RuntimeException("Tài khoản của bạn đã bị khóa bởi Quản trị viên hệ thống");
    } catch (AuthenticationException e) {
        throw new RuntimeException("Đăng nhập thất bại: " + e.getMessage());
    }
}
}