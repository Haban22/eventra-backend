package com.eventra.backend.module.notification.dto;

import com.eventra.backend.module.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating a notification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "User ID is required")
    private Long userId;
}
