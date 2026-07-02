package com.eventra.backend.module.notification.service;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.notification.dto.NotificationResponse;
import com.eventra.backend.module.notification.entity.Notification;
import com.eventra.backend.module.notification.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Called from other modules (booking payment/refund, event approval, organizer
// approval, badge unlocks, direct messages, payout decisions) — persists the
// notification and pushes it in real time over /user/{userId}/queue/notifications.
// There is no separate "read" trigger from the WS side; REST is the only write path,
// same pattern as the messaging module.
@Service
public class NotificationService {
    private final NotificationRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository repository, SimpMessagingTemplate messagingTemplate) {
        this.repository = repository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public NotificationResponse notify(UUID userId, String type, String title, String message, String actionUrl) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setActionUrl(actionUrl);
        notification = repository.save(notification);

        NotificationResponse response = NotificationResponse.from(notification);
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(NotificationResponse::from).toList();
    }

    @Transactional
    public void markRead(UUID userId, UUID notificationId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Notification not found"));
        if (!notification.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not own this notification");
        }
        notification.setRead(true);
        repository.save(notification);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        repository.markAllRead(userId);
    }

    @Transactional
    public void delete(UUID userId, UUID notificationId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Notification not found"));
        if (!notification.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not own this notification");
        }
        repository.delete(notification);
    }
}
