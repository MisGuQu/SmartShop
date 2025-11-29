package com.smartshop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.smartshop.dto.auth.*;
import com.smartshop.entity.enums.RoleName;
import com.smartshop.entity.user.Role;
import com.smartshop.entity.user.User;
import com.smartshop.repository.RoleRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final Cloudinary cloudinary;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.web.base-url:http://localhost:8080}")
    private String appBaseUrl;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuthenticationManager authenticationManager,
                       Cloudinary cloudinary) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
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

    // 7️⃣ Login Google OAuth2 – nhận idToken từ FE
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        if (request == null || request.getIdToken() == null || request.getIdToken().trim().isEmpty()) {
            throw new RuntimeException("Google token không được để trống");
        }
        
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken();
            Map<String, Object> googleUser = restTemplate.getForObject(url, Map.class);

            if (googleUser == null) {
                throw new RuntimeException("Không thể xác thực token từ Google. Vui lòng thử lại.");
            }
            
            // Check for error in response
            if (googleUser.containsKey("error")) {
                String error = String.valueOf(googleUser.get("error"));
                String errorDescription = googleUser.containsKey("error_description") 
                    ? String.valueOf(googleUser.get("error_description")) 
                    : error;
                throw new RuntimeException("Google token không hợp lệ: " + errorDescription);
            }
            
            // Verify email is verified
            Object emailVerifiedObj = googleUser.get("email_verified");
            if (emailVerifiedObj == null || !"true".equals(String.valueOf(emailVerifiedObj))) {
                throw new RuntimeException("Email Google chưa được xác thực. Vui lòng xác thực email trước khi đăng nhập.");
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
        } catch (org.springframework.web.client.RestClientException e) {
            throw new RuntimeException("Không thể kết nối đến Google để xác thực. Vui lòng kiểm tra kết nối internet và thử lại.", e);
        } catch (RuntimeException e) {
            // Re-throw our custom exceptions
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xử lý đăng nhập Google: " + e.getMessage(), e);
        }
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
    
    // Change password
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }
        
        // Check if new password matches confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }
        
        // Check if new password is different from current password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
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


