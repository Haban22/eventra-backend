package com.eventra.backend.module.notification.service;

import com.eventra.backend.common.exception.ResourceNotFoundException;
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

import java.util.List;

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
     *
     * <p>Uses {@code getReferenceById} instead of {@code findById} to avoid an extra SELECT.
     * The FK constraint in the DB guarantees integrity; if the user doesn't exist the INSERT
     * will throw a {@code DataIntegrityViolationException} which the global handler catches.</p>
     */
    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getUserId()));

        Notification notification = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .user(user)
                .build();

        return NotificationResponse.fromEntity(notificationRepository.save(notification));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolves all user references as JPA proxies via {@code getReferenceById} and
     * delegates to {@code saveAll} for a single batch INSERT, avoiding N round-trips.</p>
     */
    @Override
    @Transactional
    public void createBulkNotifications(List<NotificationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        List<Notification> notifications = requests.stream()
                .map(req -> Notification.builder()
                        .title(req.getTitle())
                        .message(req.getMessage())
                        .type(req.getType())
                        // getReferenceById returns a proxy without a SELECT — the DB FK
                        // constraint enforces existence at INSERT time.
                        .user(userRepository.getReferenceById(req.getUserId()))
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationPageResponse getUserNotifications(Long userId, Pageable pageable) {
        Page<NotificationResponse> page = notificationRepository
                .findByUserId(userId, pageable)
                .map(NotificationResponse::fromEntity);
        return NotificationPageResponse.fromPage(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationPageResponse getUnreadNotifications(Long userId, Pageable pageable) {
        Page<NotificationResponse> page = notificationRepository
                .findByUserIdAndIsReadFalse(userId, pageable)
                .map(NotificationResponse::fromEntity);
        return NotificationPageResponse.fromPage(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countUnread(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * {@inheritDoc}
     *
     * <p>No-op if the notification is already read to avoid a redundant dirty write.</p>
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

    /**
     * Fetches a notification and verifies ownership in a single query.
     *
     * <p>Returns 404 regardless of whether the notification doesn't exist or belongs
     * to a different user. This avoids leaking the existence of other users' notifications
     * (IDOR prevention).</p>
     *
     * @param notificationId notification ID
     * @param userId         authenticated user ID
     * @return the owned notification
     * @throws ResourceNotFoundException if not found or not owned by the user
     */
    private Notification findOwnedNotification(Long notificationId, Long userId) {
        return notificationRepository.findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found: " + notificationId));
    }
}
