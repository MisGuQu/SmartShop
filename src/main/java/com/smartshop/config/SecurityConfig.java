package com.smartshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * Bean mã hóa mật khẩu (sử dụng BCrypt)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager cho phép xác thực người dùng
     * Ví dụ: tạo sẵn 1 admin và 1 user để test
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http, PasswordEncoder encoder) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .inMemoryAuthentication()
                .withUser("admin").password(encoder.encode("admin123")).roles("ADMIN")
                .and()
                .withUser("user").password(encoder.encode("user123")).roles("USER")
                .and().and().build();
    }

    /**
     * SecurityFilterChain - phân quyền truy cập
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // Tắt CSRF để thử nghiệm API REST dễ dàng
            .authorizeHttpRequests(auth -> auth
                // Chỉ admin mới được gọi các API /api/admin/**
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Public API cho đăng nhập, đăng ký, xem sản phẩm
                .requestMatchers("/api/auth/**", "/api/products/**").permitAll()
                // Các request khác yêu cầu login
                .anyRequest().authenticated()
            )
            .httpBasic(); // Sử dụng Basic Auth (có thể thay bằng JWT trong thực tế)
        return http.build();
    }
}
