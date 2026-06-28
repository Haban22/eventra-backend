package com.eventra.backend.module.notification.valueobject;

import lombok.Value;

/**
 * Immutable value object representing the resolved content of a notification.
 * Produced by {@code TemplateService} after substituting variables into a template.
 */
@Value
public class NotificationContent {

    /** Resolved notification title after variable substitution. */
    String title;

    /** Resolved notification message body after variable substitution. */
    String message;
}
