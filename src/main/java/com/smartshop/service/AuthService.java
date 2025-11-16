package com.smartshop.service;

import com.smartshop.dto.auth.AuthResult;
import com.smartshop.dto.auth.LoginRequest;
import com.smartshop.dto.auth.RegisterRequest;
import com.smartshop.entity.user.Role;
import com.smartshop.entity.user.User;
import com.smartshop.exception.AuthException;
import com.smartshop.repository.RoleRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public User register(RegisterRequest request) {
        validateRegisterRequest(request);

        String normalizedEmail = request.getEmail().toLowerCase().trim();
        
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new AuthException("Email đã được sử dụng. Vui lòng chọn email khác.");
        }

        Role userRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseGet(() -> roleRepository.save(new Role(null, DEFAULT_ROLE)));

        String username = generateUniqueUsername(buildUsernameCandidate(request, normalizedEmail));

        User user = User.builder()
                .email(normalizedEmail)
                .username(username)
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(List.of(userRole))
                .isActive(true)
                .build();

        try {
            return userRepository.save(user);
        } catch (Exception ex) {
            log.error("Lỗi khi đăng ký người dùng - Exception type: {}, Message: {}", 
                    ex.getClass().getName(), ex.getMessage(), ex);
            // Kiểm tra nếu là lỗi constraint violation
            if (ex.getMessage() != null && 
                (ex.getMessage().contains("Duplicate entry") || 
                 ex.getMessage().contains("constraint") ||
                 ex.getMessage().contains("unique"))) {
                throw new AuthException("Email hoặc tên đăng nhập đã được sử dụng. Vui lòng chọn email khác.");
            }
            throw new AuthException("Không thể đăng ký tài khoản. Vui lòng thử lại sau.");
        }
    }

    public AuthResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new AuthException("Email hoặc mật khẩu không đúng."));

        if (!user.isActive()) {
            throw new AuthException("Tài khoản đã bị vô hiệu hóa.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword())
        );

        User principal = (User) authentication.getPrincipal();
        String token = jwtTokenService.generateToken(principal);
        return AuthResult.builder()
                .token(token)
                .user(principal)
                .build();
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthException("Mật khẩu xác nhận không khớp.");
        }
    }

    private String buildUsernameCandidate(RegisterRequest request, String normalizedEmail) {
        if (StringUtils.hasText(request.getFullName())) {
            String candidate = request.getFullName()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]", "");
            if (StringUtils.hasText(candidate)) {
                return candidate;
            }
        }
        int atIndex = normalizedEmail.indexOf('@');
        return atIndex > 0 ? normalizedEmail.substring(0, atIndex) : normalizedEmail;
    }

    private String generateUniqueUsername(String baseUsername) {
        String candidate = baseUsername;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = baseUsername + suffix;
            suffix++;
        }
        return candidate;
    }
}
