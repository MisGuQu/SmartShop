package com.smartshop.service;

import com.smartshop.dto.auth.AuthResult;
import com.smartshop.dto.auth.ForgotPasswordRequest;
import com.smartshop.dto.auth.LoginRequest;
import com.smartshop.dto.auth.RegisterRequest;
import com.smartshop.dto.auth.ResetPasswordRequest;
import com.smartshop.entity.enums.AuthProvider;
import com.smartshop.entity.user.PasswordResetToken;
import com.smartshop.entity.user.Role;
import com.smartshop.entity.user.User;
import com.smartshop.exception.AccountLockedException;
import com.smartshop.exception.AuthException;
import com.smartshop.repository.PasswordResetTokenRepository;
import com.smartshop.repository.RoleRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final JavaMailSender mailSender;

    @Value("${app.web.base-url:http://localhost:8080}")
    private String baseUrl;

    public User register(RegisterRequest request) {
        validateRegisterRequest(request);

        String normalizedEmail = request.getEmail().toLowerCase().trim();
        
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new AuthException("Email đã được sử dụng. Vui lòng chọn email khác.");
        }

        // Kiểm tra username trùng lặp (username = email)
        if (userRepository.existsByUsername(normalizedEmail)) {
            throw new AuthException("Email đã được sử dụng. Vui lòng chọn email khác.");
        }

        Role userRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseGet(() -> roleRepository.save(new Role(null, DEFAULT_ROLE)));

        User user = User.builder()
                .email(normalizedEmail)
                .username(normalizedEmail)
                .fullName(request.getFullName().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(List.of(userRole))
                .isActive(true)
                .emailVerified(true)
                .authProvider(AuthProvider.LOCAL)
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

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new AuthException("Tài khoản đã được liên kết với Google. Vui lòng đăng nhập bằng Google.");
        }

        if (!user.isAccountNonLocked()) {
            throw new AccountLockedException("Tài khoản tạm khóa do nhập sai nhiều lần.", user.getAccountLockedUntil());
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword())
            );

            User principal = (User) authentication.getPrincipal();
            principal.setFailedLoginAttempts(0);
            principal.setAccountLockedUntil(null);
            principal.setLastLoginAt(LocalDateTime.now());
            userRepository.save(principal);

            String token = jwtTokenService.generateToken(principal);
            return AuthResult.builder()
                    .token(token)
                    .user(principal)
                    .build();

        } catch (BadCredentialsException | UsernameNotFoundException ex) {
            registerFailedAttempt(user);
            throw new AuthException("Email hoặc mật khẩu không đúng.");
        }
    }

    public void initiatePasswordReset(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase();
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            try {
                createAndSendResetToken(user);
            } catch (Exception e) {
                log.error("Không thể gửi email đặt lại mật khẩu", e);
                throw new AuthException("Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau.");
            }
        }, () -> log.warn("Yêu cầu đặt lại mật khẩu cho email không tồn tại: {}", email));
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (!StringUtils.hasText(request.getToken())) {
            throw new AuthException("Liên kết đặt lại mật khẩu không hợp lệ.");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthException("Mật khẩu xác nhận không khớp.");
        }

        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new AuthException("Liên kết đặt lại mật khẩu không hợp lệ hoặc đã được sử dụng."));

        if (token.isUsed()) {
            throw new AuthException("Liên kết đặt lại mật khẩu đã được sử dụng.");
        }

        if (token.isExpired()) {
            throw new AuthException("Liên kết đặt lại mật khẩu đã hết hạn.");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepository.save(user);

        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);
        passwordResetTokenRepository.deleteByUser(user);
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthException("Mật khẩu xác nhận không khớp.");
        }
    }

    private void registerFailedAttempt(User user) {
        int failed = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(failed);
        if (failed >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLockedUntil(LocalDateTime.now().plus(LOCK_DURATION));
        }
        userRepository.save(user);
    }

    private void createAndSendResetToken(User user) {
        passwordResetTokenRepository.deleteByUser(user);

        PasswordResetToken token = PasswordResetToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        passwordResetTokenRepository.save(token);

        String resetLink = baseUrl + "/auth/reset-password?token=" + token.getToken();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Đặt lại mật khẩu SmartShop");
        message.setText("Xin chào " + user.getFullName() + ",\n\n" +
                "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. \n" +
                "Hãy nhấn vào liên kết sau để tạo mật khẩu mới: " + resetLink + "\n\n" +
                "Liên kết sẽ hết hạn sau 60 phút. Nếu bạn không yêu cầu, hãy bỏ qua email này." +
                "\n\nTrân trọng,\nSmartShop Team");

        mailSender.send(message);
    }
}
