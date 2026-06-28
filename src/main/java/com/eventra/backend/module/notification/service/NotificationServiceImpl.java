package com.eventra.backend.module.notification.service;

import com.eventra.backend.common.exception.ResourceNotFoundException;
import com.eventra.backend.common.exception.UnauthorizedException;
import com.eventra.backend.module.notification.dto.NotificationPageResponse;
import com.eventra.backend.module.notification.dto.NotificationRequest;
import com.eventra.backend.module.notification.dto.NotificationResponse;
import com.eventra.backend.module.notification.entity.Notification;
import com.eventra.backend.module.notification.repository.NotificationRepository;
import com.eventra.backend.module.user.entity.User;
import com.eventra.backend.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link NotificationService}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Notification notification = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .isRead(false)
                .user(user)
                .build();

        Notification saved = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationPageResponse getUserNotifications(Long userId, Pageable pageable) {
        Page<NotificationResponse> page = notificationRepository.findByUserId(userId, pageable)
                .map(NotificationResponse::fromEntity);
        return NotificationPageResponse.fromPage(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationPageResponse getUnreadNotifications(Long userId, Pageable pageable) {
        Page<NotificationResponse> page = notificationRepository.findByUserIdAndIsReadFalse(userId, pageable)
                .map(NotificationResponse::fromEntity);
        return NotificationPageResponse.fromPage(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = findOwnedNotification(notificationId, userId);

        if (!notification.isRead()) {
            notification.setRead(true);
            notification = notificationRepository.save(notification);
        }

        return NotificationResponse.fromEntity(notification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = findOwnedNotification(notificationId, userId);
        notificationRepository.delete(notification);
    }

    private Notification findOwnedNotification(Long notificationId, Long userId) {
        return notificationRepository.findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new UnauthorizedException(
                        "Notification not found or you do not have access to notification id: " + notificationId));
    }
}
