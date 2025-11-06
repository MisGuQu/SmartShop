package com.smartshop.security.oauth;

import com.smartshop.entity.enums.AuthProvider;
import com.smartshop.entity.user.Role;
import com.smartshop.entity.user.User;
import com.smartshop.repository.RoleRepository;
import com.smartshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository,
                                   RoleRepository roleRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private static final String DEFAULT_ROLE = "ROLE_USER";

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException("Email not found from provider");
        }

        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        User user = existingUserOpt.orElseGet(() -> registerOAuthUser(attributes, email));

        if (existingUserOpt.isPresent() && user.getAuthProvider() == AuthProvider.LOCAL) {
            throw new OAuth2AuthenticationException("Tài khoản đã được đăng ký bằng email & mật khẩu. Vui lòng đăng nhập theo cách đó.");
        }

        if (user.getAuthProvider() != AuthProvider.GOOGLE) {
            user.setAuthProvider(AuthProvider.GOOGLE);
        }

        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(DEFAULT_ROLE)),
                attributes,
                "email"
        );
    }

    private User registerOAuthUser(Map<String, Object> attributes, String email) {
        String fullName = (String) attributes.getOrDefault("name", email);
        String picture = (String) attributes.get("picture");

        Role userRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseGet(() -> roleRepository.save(new Role(null, DEFAULT_ROLE)));

        return userRepository.save(User.builder()
                .email(email)
                .username(email)
                .fullName(fullName)
                .avatarPublicId(picture)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .roles(List.of(userRole))
                .isActive(true)
                .authProvider(AuthProvider.GOOGLE)
                .emailVerified(true)
                .build());
    }
}

