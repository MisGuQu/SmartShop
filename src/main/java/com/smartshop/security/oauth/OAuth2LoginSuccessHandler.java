package com.smartshop.security.oauth;

import com.smartshop.entity.user.User;
import com.smartshop.repository.UserRepository;
import com.smartshop.security.JwtCookieService;
import com.smartshop.security.JwtTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenService jwtTokenService;
    private final JwtCookieService jwtCookieService;
    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(JwtTokenService jwtTokenService,
                                     JwtCookieService jwtCookieService,
                                     UserRepository userRepository) {
        this.jwtTokenService = jwtTokenService;
        this.jwtCookieService = jwtCookieService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        String email = null;
        if (principal instanceof OAuth2User oAuth2User) {
            email = (String) oAuth2User.getAttributes().get("email");
        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        }

        if (email != null) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String token = jwtTokenService.generateToken(user);
                ResponseCookie cookie = jwtCookieService.buildAccessTokenCookie(token);
                response.addHeader("Set-Cookie", cookie.toString());
            }
        }

        response.sendRedirect("/");
    }
}

