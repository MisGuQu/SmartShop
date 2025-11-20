package com.smartshop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.smartshop.dto.auth.*;
import com.smartshop.entity.enums.RoleName;
import com.smartshop.entity.user.PasswordResetToken;
import com.smartshop.entity.user.Role;
import com.smartshop.entity.user.User;
import com.smartshop.repository.PasswordResetTokenRepository;
import com.smartshop.repository.RoleRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;
    private final Cloudinary cloudinary;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.web.base-url:http://localhost:8080}")
    private String appBaseUrl;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuthenticationManager authenticationManager,
                       JavaMailSender mailSender,
                       Cloudinary cloudinary) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.mailSender = mailSender;
        this.cloudinary = cloudinary;
    }

    // 5️⃣ Đăng nhập – trả JWT token
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        String jwt = tokenProvider.generateToken(user);

        return AuthResponse.builder()
                .token(jwt)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .build();
    }

    // 4️⃣ Đăng ký – BCrypt
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER.name())
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleName.ROLE_CUSTOMER.name());
                    return roleRepository.save(newRole);
                });

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .phone(registerRequest.getPhone())
                .isActive(true)
                .roles(new ArrayList<>())
                .build();

        user.getRoles().add(customerRole);
        user = userRepository.save(user);

        String jwt = tokenProvider.generateToken(user);

        return AuthResponse.builder()
                .token(jwt)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .build();
    }

    // 6️⃣ Quên mật khẩu – gửi email reset
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();
        passwordResetTokenRepository.save(resetToken);

        String resetLink = appBaseUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reset mật khẩu SmartShop");
        message.setText("Nhấp vào link sau để reset mật khẩu: " + resetLink);
        mailSender.send(message);
    }

    // 6️⃣ Reset mật khẩu – dùng token
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        if (resetToken.isExpired() || resetToken.isUsed()) {
            throw new RuntimeException("Token đã hết hạn hoặc đã sử dụng");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);
    }

    // 7️⃣ Login Google OAuth2 – nhận idToken từ FE
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken();
        Map<String, Object> googleUser = restTemplate.getForObject(url, Map.class);

        if (googleUser == null || !"true".equals(String.valueOf(googleUser.get("email_verified")))) {
            throw new RuntimeException("Google token không hợp lệ");
        }

        String email = String.valueOf(googleUser.get("email"));
        Object nameObj = googleUser.get("name");
        String name = nameObj != null ? nameObj.toString() : email;
        Object pictureObj = googleUser.get("picture");
        String pictureUrl = pictureObj != null ? pictureObj.toString() : null;

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER.name())
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName(RoleName.ROLE_CUSTOMER.name());
                        return roleRepository.save(newRole);
                    });

            User newUser = User.builder()
                    .username(email)
                    .email(email)
                    .fullName(name)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .isActive(true)
                    .roles(new ArrayList<>())
                    .build();
            newUser.getRoles().add(customerRole);
            
            // Set avatar from Google if available
            if (pictureUrl != null && !pictureUrl.isEmpty()) {
                newUser.setAvatar(pictureUrl);
            }
            
            return userRepository.save(newUser);
        });
        
        // Update user info if needed (for existing users)
        boolean updated = false;
        if (user.getFullName() == null || user.getFullName().isEmpty()) {
            user.setFullName(name);
            updated = true;
        }
        if ((user.getAvatar() == null || user.getAvatar().isEmpty()) && pictureUrl != null && !pictureUrl.isEmpty()) {
            user.setAvatar(pictureUrl);
            updated = true;
        }
        if (updated) {
            userRepository.save(user);
        }

        String jwt = tokenProvider.generateToken(user);

        return AuthResponse.builder()
                .token(jwt)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .build();
    }
    
    // Update user profile
    public AuthResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // Check if email is being changed and if it's already taken
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email đã được sử dụng");
            }
            user.setEmail(request.getEmail());
        }
        
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        user = userRepository.save(user);
        
        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .build();
    }
    
    // Upload avatar
    public AuthResponse uploadAvatar(String username, MultipartFile file) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
        
        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 5MB");
        }
        
        try {
            // Upload to Cloudinary
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "smartshop/users",
                            "resource_type", "image",
                            "format", "jpg"
                    ));
            
            String url = (String) uploadResult.get("secure_url");
            if (url == null) {
                throw new RuntimeException("Failed to get image URL from Cloudinary");
            }
            
            user.setAvatar(url);
            user = userRepository.save(user);
            
            return AuthResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .phone(user.getPhone())
                    .avatar(user.getAvatar())
                    .roles(user.getRoles().stream()
                            .map(Role::getName)
                            .collect(Collectors.toList()))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }
}


