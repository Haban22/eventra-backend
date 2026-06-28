package com.eventra.backend.module.notification.service;

import com.eventra.backend.module.notification.entity.NotificationTemplate;
import com.eventra.backend.module.notification.enums.NotificationType;
import com.eventra.backend.module.notification.repository.NotificationTemplateRepository;
import com.eventra.backend.module.notification.valueobject.NotificationContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Resolves notification content from stored {@link NotificationTemplate} records.
 *
 * <p>Variable substitution uses a simple {@code {key}} placeholder format.
 * Example template: "Your booking for {eventTitle} is confirmed."
 * With variables: {"eventTitle": "Spring Gala"} produces:
 * "Your booking for Spring Gala is confirmed."</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;

    /**
     * Resolves a {@link NotificationContent} by substituting the provided variables
     * into the template registered for the given notification type.
     *
     * <p>Returns {@code Optional.empty()} if no template is configured for the type,
     * allowing callers to fall back to hardcoded messages.</p>
     *
     * @param type      the notification type to look up
     * @param variables a map of placeholder keys to replacement values
     * @return resolved content, or empty if no template is found
     */
    public Optional<NotificationContent> resolve(NotificationType type, Map<String, String> variables) {
        return templateRepository.findByNotificationType(type)
                .map(template -> new NotificationContent(
                        substitute(template.getTitleTemplate(), variables),
                        substitute(template.getMessageTemplate(), variables)
                ));
    }

    /**
     * Substitutes all {@code {key}} placeholders in a template string with values from the map.
     *
     * @param template  the template string containing placeholders
     * @param variables substitution values keyed by placeholder name
     * @return the resolved string
     */
    private String substitute(String template, Map<String, String> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
