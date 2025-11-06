package com.smartshop.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class JwtCookieService {

    @Value("${app.security.jwt.cookie-name:SMARTSHOP_TOKEN}")
    private String cookieName;

    @Value("${app.security.jwt.cookie-domain:localhost}")
    private String cookieDomain;

    @Value("${app.security.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${app.security.jwt.cookie-samesite:Strict}")
    private String sameSite;

    private final JwtTokenService jwtTokenService;

    public ResponseCookie buildAccessTokenCookie(String token) {
        long maxAgeSeconds = jwtTokenService.getAccessTokenValidityMs() / 1000;
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds > 0 ? maxAgeSeconds : 3600));

        if (!"localhost".equalsIgnoreCase(cookieDomain) && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        return builder.build();
    }

    public ResponseCookie buildDeleteCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ZERO);

        if (!"localhost".equalsIgnoreCase(cookieDomain) && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }
}

