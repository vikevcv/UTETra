package com.ute.trasua.config;

import com.ute.trasua.security.*;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint entryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DRIVER") || a.getAuthority().equals("DRIVER"))) {
                        response.sendRedirect(request.getContextPath() + "/shipper?accessDenied=true");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/?accessDenied=true");
                    }
                }))
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 1. Cho phép tất cả các dispatch nội bộ (forward tới JSP, include, error)
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR).permitAll()
                // 2. Cho phép các URL công khai và tài nguyên tĩnh
                .requestMatchers(
                    "/",
                    "/portal",
                    "/auth/**",
                    "/cart", "/cart/**",
                    "/api/v1/cart/**",
                    "/api/v1/promotions/**",
                    "/api/v1/product-options/active",
                    "/pos", "/pos/**",
                    "/branch/pos", "/branch/pos/**",
                    "/branch/kitchen", "/branch/kitchen/**",
                    "/branch/handover", "/branch/handover/**",
                    "/branch/menu-status", "/branch/menu-status/**",
                    "/api/pos/**",
                    "/api/kitchen/**",
                    "/api/handover/**",
                    "/api/branch-menu/**",
                    "/shipper", "/shipper/**",
                    "/ws-kds/**",
                    "/ws-kds-native/**",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico",
                    "/h2-console/**",
                    "/WEB-INF/**"
                ).permitAll()
                // 3. Phân quyền chặt chẽ theo vai trò
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                .requestMatchers("/branch", "/branch/**").hasAnyRole("ADMIN", "MANAGER")
                .anyRequest().authenticated())
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(fo -> fo.disable()))
            .addFilterBefore(jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}