package com.smartshop.controller.api;

import com.smartshop.dto.notification.NotificationDTO;
import com.smartshop.entity.User;
import com.smartshop.response.ApiResponse;
import com.smartshop.service.NotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 🔔 Lấy danh sách thông báo của người dùng hiện tại (có phân trang)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        Page<NotificationDTO> notifications = notificationService.getNotifications(user.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * ✅ Đánh dấu 1 thông báo là "đã đọc"
     */
    @PostMapping("/read/{id}")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        notificationService.markAsRead(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã đánh dấu thông báo là đã đọc"));
    }
}
