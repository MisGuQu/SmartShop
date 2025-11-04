package com.smartshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartshop.entity.user.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByIsActive(boolean isActive);
}