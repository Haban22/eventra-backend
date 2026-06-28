package com.eventra.backend.module.notification.repository;

import com.eventra.backend.module.notification.entity.NotificationPreference;
import com.eventra.backend.module.notification.enums.NotificationChannel;
import com.eventra.backend.module.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link NotificationPreference} entities.
 */
@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    /**
     * Finds all preferences for a given user.
     *
     * @param userId the user ID
     * @return list of preferences
     */
    List<NotificationPreference> findByUserId(Long userId);

    /**
     * Finds a specific preference for a user by notification type and channel.
     *
     * @param userId           the user ID
     * @param notificationType the notification type
     * @param channel          the delivery channel
     * @return the preference if it exists
     */
    Optional<NotificationPreference> findByUserIdAndNotificationTypeAndChannel(
            Long userId,
            NotificationType notificationType,
            NotificationChannel channel
    );
}
