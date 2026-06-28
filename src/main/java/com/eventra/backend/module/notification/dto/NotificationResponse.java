package com.eventra.backend.module.notification.dto;

import com.eventra.backend.module.notification.entity.Notification;
import com.eventra.backend.module.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response payload representing a user notification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private boolean isRead;
    private LocalDateTime createdAt;
    private Long userId;

    /**
     * Maps a {@link Notification} entity to its response representation.
     *
     * @param notification persisted notification entity
     * @return mapped response DTO
     */
    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .userId(notification.getUser().getId())
                .build();
    }
}
