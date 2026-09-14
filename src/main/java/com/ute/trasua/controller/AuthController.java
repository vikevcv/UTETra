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
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.initiateRegistration(request);
            redirectAttributes.addFlashAttribute("email", request.getEmail());
            redirectAttributes.addFlashAttribute("successMessage", "Mã OTP đã được gửi đến email của bạn.");
            return "redirect:/auth/verify-otp";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    // VERIFY OTP
    @GetMapping("/verify-otp")
    public String verifyOtpPage(Model model, @ModelAttribute("email") String email) {
        if (email == null || email.isEmpty()) {
            return "redirect:/auth/register";
        }
        OtpVerificationRequest otpRequest = new OtpVerificationRequest();
        otpRequest.setEmail(email);
        model.addAttribute("otpRequest", otpRequest);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@Valid @ModelAttribute("otpRequest") OtpVerificationRequest request,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("email", request.getEmail());
            return "auth/verify-otp";
        }

        try {
            authService.verifyOtpAndRegister(request.getEmail(), request.getOtp());
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("email", request.getEmail());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/verify-otp";
        }
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
