package com.smartshop.service;

import com.smartshop.entity.notification.Notification;
import com.smartshop.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> getNotificationsByUser(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Notification markAsRead(Long userId, Long notificationId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(notificationId, "notificationId must not be null");

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Permission denied");
        }

        notification.setRead(true);
        return notificationRepository.save(notification);
    }
}
