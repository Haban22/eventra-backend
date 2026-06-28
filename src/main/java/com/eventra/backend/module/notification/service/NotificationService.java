package com.eventra.backend.module.notification.service;

import com.eventra.backend.module.notification.dto.NotificationPageResponse;
import com.eventra.backend.module.notification.dto.NotificationRequest;
import com.eventra.backend.module.notification.dto.NotificationResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for notification CRUD and read-state management.
 */
public interface NotificationService {

    /**
     * Creates a new notification for a user.
     *
     * @param request notification creation payload
     * @return created notification response
     */
    NotificationResponse createNotification(NotificationRequest request);

    /**
     * Retrieves paginated notifications for the authenticated user.
     *
     * @param userId   authenticated user ID
     * @param pageable pagination parameters
     * @return paginated notifications
     */
    NotificationPageResponse getUserNotifications(Long userId, Pageable pageable);

    /**
     * Retrieves paginated unread notifications for the authenticated user.
     *
     * @param userId   authenticated user ID
     * @param pageable pagination parameters
     * @return paginated unread notifications
     */
    NotificationPageResponse getUnreadNotifications(Long userId, Pageable pageable);

    /**
     * Marks a single notification as read.
     *
     * @param notificationId notification ID
     * @param userId         authenticated user ID
     * @return updated notification response
     */
    NotificationResponse markAsRead(Long notificationId, Long userId);

    /**
     * Marks all unread notifications as read for a user.
     *
     * @param userId authenticated user ID
     * @return number of notifications marked as read
     */
    int markAllAsRead(Long userId);

    /**
     * Deletes a notification owned by the authenticated user.
     *
     * @param notificationId notification ID
     * @param userId         authenticated user ID
     */
    void deleteNotification(Long notificationId, Long userId);
}
