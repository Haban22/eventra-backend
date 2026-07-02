package com.eventra.backend.module.notification.dto;

import com.eventra.backend.module.notification.entity.Notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        String type,
        String title,
        String message,
        String actionUrl,
        boolean isRead,
        boolean aiGenerated,
        Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getUserId(), n.getType(), n.getTitle(), n.getMessage(),
                n.getActionUrl(), n.isRead(), n.isAiGenerated(), n.getCreatedAt()
        );
    }
}
