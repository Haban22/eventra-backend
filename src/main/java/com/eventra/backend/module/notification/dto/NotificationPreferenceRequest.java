package com.eventra.backend.module.notification.dto;

import com.eventra.backend.module.notification.enums.NotificationChannel;
import com.eventra.backend.module.notification.enums.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for updating a user's notification preference for a specific
 * notification type and delivery channel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRequest {

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    @NotNull(message = "Notification channel is required")
    private NotificationChannel channel;

    @NotNull(message = "Enabled flag is required")
    private Boolean enabled;
}
