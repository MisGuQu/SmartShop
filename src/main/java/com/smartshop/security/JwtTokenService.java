package com.smartshop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtTokenService {

    @Value("${app.security.jwt.secret}")
    private String secret;

    @Value("${app.security.jwt.expiration:3600000}")
    @Getter
    private long accessTokenValidityMs;

    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException ex) {
            log.warn("JWT secret is not Base64 encoded. Using raw bytes.");
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            log.warn("JWT secret is shorter than recommended. Deriving 256-bit key.");
            keyBytes = deriveKey(keyBytes);
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityMs);
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setClaims(Map.of("roles", roles))
                .setSubject(principal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    public String extractEmail(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
    }

    private byte[] deriveKey(byte[] input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (java.security.NoSuchAlgorithmException ex) {
            log.error("SHA-256 algorithm not available, falling back to padding.");
            byte[] padded = new byte[32];
            System.arraycopy(input, 0, padded, 0, Math.min(input.length, 32));
            return padded;
        }
    }
}

