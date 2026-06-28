package com.eventra.backend.module.notification.service;

import com.eventra.backend.module.notification.dto.NotificationPageResponse;
import com.eventra.backend.module.notification.dto.NotificationRequest;
import com.eventra.backend.module.notification.dto.NotificationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service contract for notification CRUD, bulk creation, and read-state management.
 */
public interface NotificationService {

    /**
     * Creates a single notification for a user.
     * Intended for internal service use only — userId comes from a trusted context.
     *
     * @param request notification creation payload
     * @return created notification response
     */
    NotificationResponse createNotification(NotificationRequest request);

    /**
     * Creates multiple notifications in a single batch INSERT.
     * Use this for fan-out scenarios (e.g., notifying all event attendees)
     * to avoid N individual INSERT statements.
     *
     * @param requests list of notification creation payloads
     */
    void createBulkNotifications(List<NotificationRequest> requests);

    /**
     * Retrieves paginated notifications for the authenticated user.
     *
     * @param userId   authenticated user ID
     * @param pageable pagination and sort parameters
     * @return paginated notifications
     */
    NotificationPageResponse getUserNotifications(Long userId, Pageable pageable);

    /**
     * Retrieves paginated unread notifications for the authenticated user.
     *
     * @param userId   authenticated user ID
     * @param pageable pagination and sort parameters
     * @return paginated unread notifications
     */
    NotificationPageResponse getUnreadNotifications(Long userId, Pageable pageable);

    /**
     * Returns the count of unread notifications for a user.
     * Lightweight query suitable for badge/indicator display.
     *
     * @param userId authenticated user ID
     * @return count of unread notifications
     */
    long countUnread(Long userId);

    /**
     * Marks a single notification as read.
     *
     * @param notificationId notification ID
     * @param userId         authenticated user ID (ownership check)
     * @return updated notification response
     */
    NotificationResponse markAsRead(Long notificationId, Long userId);

    /**
     * Marks all unread notifications as read for a user atomically.
     *
     * @param userId authenticated user ID
     * @return number of notifications marked as read
     */
    int markAllAsRead(Long userId);

    /**
     * Deletes a notification owned by the authenticated user.
     *
     * @param notificationId notification ID
     * @param userId         authenticated user ID (ownership check)
     */
    void deleteNotification(Long notificationId, Long userId);
}
