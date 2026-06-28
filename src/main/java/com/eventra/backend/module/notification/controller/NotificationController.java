package com.eventra.backend.module.notification.controller;

import com.eventra.backend.common.response.ApiResponse;
import com.eventra.backend.module.notification.dto.NotificationPageResponse;
import com.eventra.backend.module.notification.dto.NotificationResponse;
import com.eventra.backend.module.notification.service.NotificationService;
import com.eventra.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for user notification management.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Returns paginated notifications for the authenticated user.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated notification list
     */
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        NotificationPageResponse response = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Returns paginated unread notifications for the authenticated user.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated unread notification list
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        NotificationPageResponse response = notificationService.getUnreadNotifications(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Marks a single notification as read.
     *
     * @param id notification ID
     * @return updated notification
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        NotificationResponse response = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Marks all unread notifications as read for the authenticated user.
     *
     * @return count of notifications updated
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        int updatedCount = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("updatedCount", updatedCount)));
    }

    /**
     * Deletes a notification owned by the authenticated user.
     *
     * @param id notification ID
     * @return empty success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
