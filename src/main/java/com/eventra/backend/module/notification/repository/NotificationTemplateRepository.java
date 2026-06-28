package com.eventra.backend.module.notification.repository;

import com.eventra.backend.module.notification.entity.NotificationTemplate;
import com.eventra.backend.module.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for {@link NotificationTemplate} entities.
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    /**
     * Finds a template by its notification type.
     *
     * @param notificationType the notification type
     * @return the template if defined
     */
    Optional<NotificationTemplate> findByNotificationType(NotificationType notificationType);
}
