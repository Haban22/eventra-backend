package com.eventra.backend.module.notification.dto;

import com.eventra.backend.module.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal request payload for creating a notification programmatically.
 *
 * <p>This DTO is used exclusively by internal services (e.g., {@code NotificationEventService})
 * and is never accepted directly from client HTTP requests. The {@code userId} field must
 * always be sourced from a trusted internal context, never from untrusted client input.</p>
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

    /**
     * Target user ID. Must be sourced from the authenticated security context or
     * a trusted internal service call — never from external/client input.
     */
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be a positive number")
    private Long userId;
}
