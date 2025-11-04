package com.smartshop.repository;

import org.springframework.data.domain.Page;           // ĐÚNG
import org.springframework.data.domain.Pageable;       // ĐÚNG
import org.springframework.data.jpa.repository.JpaRepository;

import com.smartshop.entity.notification.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
}