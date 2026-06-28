package com.eventra.backend.module.notification.dto;

import com.eventra.backend.module.notification.entity.Notification;
import com.eventra.backend.module.notification.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response payload representing a user notification.
 *
 * <p>The {@code isRead} field uses {@code @JsonProperty("isRead")} to ensure Jackson
 * serializes it as "isRead" and not "read" (which would happen with a plain
 * {@code boolean read} field due to JavaBean getter naming conventions).</p>
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

    /**
     * Explicit JSON property name to guarantee the API contract uses "isRead"
     * regardless of Java field naming or Lombok getter generation.
     */
    @JsonProperty("isRead")
    private boolean isRead;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
