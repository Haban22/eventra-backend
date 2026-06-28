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
 *
 * <p>All endpoints are authenticated — the user ID is always resolved from the JWT
 * via {@link SecurityUtils#getCurrentUserId()}, never from client-supplied parameters.</p>
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    /** Maximum allowed page size to prevent unbounded result-set requests. */
    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationService notificationService;

    /**
     * Returns paginated notifications for the authenticated user.
     *
     * @param page zero-based page index (default 0)
     * @param size page size, capped at {@value #MAX_PAGE_SIZE} (default 20)
     * @return paginated notification list
     */
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        NotificationPageResponse response = notificationService
                .getUserNotifications(userId, buildPageable(page, size));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Returns paginated unread notifications for the authenticated user.
     *
     * @param page zero-based page index (default 0)
     * @param size page size, capped at {@value #MAX_PAGE_SIZE} (default 20)
     * @return paginated unread notification list
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        NotificationPageResponse response = notificationService
                .getUnreadNotifications(userId, buildPageable(page, size));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Returns the count of unread notifications for the authenticated user.
     * Lightweight endpoint suitable for badge/indicator display.
     *
     * @return unread notification count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
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
     * Returns 204 No Content on success — correct HTTP semantics for a DELETE with no body.
     *
     * @param id notification ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Builds a {@link Pageable} with DESC sort on {@code createdAt} and a capped page size.
     *
     * @param page requested page index
     * @param size requested page size
     * @return configured pageable
     */
    private Pageable buildPageable(int page, int size) {
        int cappedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(Math.max(page, 0), cappedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
