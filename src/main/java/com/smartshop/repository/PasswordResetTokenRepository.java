package com.smartshop.repository;

import com.smartshop.entity.user.PasswordResetToken;
import com.smartshop.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
    void deleteByExpiresAtBefore(LocalDateTime expiryThreshold);
}

