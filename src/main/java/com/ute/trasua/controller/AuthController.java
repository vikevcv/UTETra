package com.ute.trasua.controller;

import com.ute.trasua.dto.*;
import com.ute.trasua.security.JwtAuthenticationFilter;
import com.ute.trasua.security.JwtTokenProvider;
import com.ute.trasua.service.AuthService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    // LOGIN
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Sai tài khoản hoặc mật khẩu");
        }
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest request,
                        BindingResult result,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/login";
        }

        try {
            String token = authService.login(request);
            addJwtCookie(response, token);
            return "redirect:/";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/login";
        }
    }

    // REGISTER
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest request,
                           BindingResult result,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.initiateRegistration(request);
            String cleanEmail = request.getEmail().trim().toLowerCase();
            session.setAttribute("PENDING_REGISTER_EMAIL", cleanEmail);
            redirectAttributes.addFlashAttribute("email", cleanEmail);
            redirectAttributes.addFlashAttribute("successMessage", "Mã OTP đã được gửi đến email của bạn.");
            return "redirect:/auth/verify-otp";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    // VERIFY OTP
    @GetMapping("/verify-otp")
    public String verifyOtpPage(Model model,
                                @ModelAttribute("email") String email,
                                HttpSession session) {
        if (email == null || email.trim().isEmpty()) {
            email = (String) session.getAttribute("PENDING_REGISTER_EMAIL");
        }
        if (email == null || email.trim().isEmpty()) {
            return "redirect:/auth/register";
        }

        OtpVerificationRequest otpRequest = new OtpVerificationRequest();
        otpRequest.setEmail(email);
        model.addAttribute("otpRequest", otpRequest);
        model.addAttribute("email", email);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@Valid @ModelAttribute("otpRequest") OtpVerificationRequest request,
                            BindingResult result,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("email", request.getEmail());
            return "auth/verify-otp";
        }

        try {
            authService.verifyOtpAndRegister(request.getEmail(), request.getOtp());
            session.removeAttribute("PENDING_REGISTER_EMAIL");
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("email", request.getEmail());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/verify-otp";
        }
    }

    @PostMapping("/resend-register-otp")
    public String resendRegisterOtp(@RequestParam(value = "email", required = false) String email,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (email == null || email.trim().isEmpty()) {
            email = (String) session.getAttribute("PENDING_REGISTER_EMAIL");
        }
        if (email == null || email.trim().isEmpty()) {
            return "redirect:/auth/register";
        }

        try {
            authService.resendRegisterOtp(email);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("successMessage", "Mã OTP mới đã được gửi đến email của bạn.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/auth/verify-otp";
    }

    // LOGOUT
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(JwtTokenProvider.JWT_COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/auth/login";
    }

    // =========================================================================
    // FORGOT PASSWORD FLOW
    // =========================================================================

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute ForgotPasswordRequest request,
                                 BindingResult result,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/forgot-password";
        }

        try {
            authService.initiateForgotPassword(request);
            String cleanEmail = request.getEmail().trim().toLowerCase();
            session.setAttribute("PENDING_FORGOT_EMAIL", cleanEmail);
            redirectAttributes.addFlashAttribute("email", cleanEmail);
            redirectAttributes.addFlashAttribute("successMessage", "Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra hòm thư.");
            return "redirect:/auth/verify-forgot-otp";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/forgot-password";
        }
    }

    @GetMapping("/verify-forgot-otp")
    public String verifyForgotOtpPage(Model model,
                                      @ModelAttribute("email") String email,
                                      HttpSession session) {
        if (email == null || email.trim().isEmpty()) {
            email = (String) session.getAttribute("PENDING_FORGOT_EMAIL");
        }
        if (email == null || email.trim().isEmpty()) {
            return "redirect:/auth/forgot-password";
        }

        OtpVerificationRequest otpRequest = new OtpVerificationRequest();
        otpRequest.setEmail(email);
        model.addAttribute("otpRequest", otpRequest);
        model.addAttribute("email", email);
        return "auth/verify-forgot-otp";
    }

    @PostMapping("/verify-forgot-otp")
    public String verifyForgotOtp(@Valid @ModelAttribute("otpRequest") OtpVerificationRequest request,
                                  BindingResult result,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("email", request.getEmail());
            return "auth/verify-forgot-otp";
        }

        try {
            String resetToken = authService.verifyForgotPasswordOtp(request.getEmail(), request.getOtp());
            session.removeAttribute("PENDING_FORGOT_EMAIL");
            session.setAttribute("RESET_PASSWORD_TOKEN", resetToken);
            return "redirect:/auth/reset-password?token=" + resetToken;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("email", request.getEmail());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/verify-forgot-otp";
        }
    }

    @PostMapping("/resend-forgot-otp")
    public String resendForgotOtp(@RequestParam(value = "email", required = false) String email,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (email == null || email.trim().isEmpty()) {
            email = (String) session.getAttribute("PENDING_FORGOT_EMAIL");
        }
        if (email == null || email.trim().isEmpty()) {
            return "redirect:/auth/forgot-password";
        }

        try {
            authService.resendForgotPasswordOtp(email);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("successMessage", "Mã OTP mới đã được gửi đến email của bạn.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/auth/verify-forgot-otp";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(value = "token", required = false) String token,
                                    Model model,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (token == null || token.trim().isEmpty()) {
            token = (String) session.getAttribute("RESET_PASSWORD_TOKEN");
        }

        if (token == null || !authService.validateResetToken(token)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            return "redirect:/auth/forgot-password";
        }

        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setToken(token);
        model.addAttribute("resetPasswordRequest", resetRequest);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute ResetPasswordRequest request,
                                BindingResult result,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/reset-password";
        }

        try {
            authService.resetPassword(request);
            session.removeAttribute("RESET_PASSWORD_TOKEN");
            redirectAttributes.addFlashAttribute("successMessage", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập với mật khẩu mới.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/reset-password?token=" + request.getToken();
        }
    }



    // Helper
    private void addJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(JwtTokenProvider.JWT_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);   // true khi dùng HTTPS
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtExpirationMs / 1000));
        response.addCookie(cookie);
    }
}
