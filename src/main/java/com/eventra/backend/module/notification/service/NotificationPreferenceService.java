package com.eventra.backend.module.notification.service;

import com.eventra.backend.common.exception.ResourceNotFoundException;
import com.eventra.backend.module.notification.dto.NotificationPreferenceRequest;
import com.eventra.backend.module.notification.entity.NotificationPreference;
import com.eventra.backend.module.notification.repository.NotificationPreferenceRepository;
import com.eventra.backend.module.user.entity.User;
import com.eventra.backend.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages per-user notification delivery preferences.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    /**
     * Returns all notification preferences for a user.
     *
     * @param userId the authenticated user ID
     * @return list of preferences
     */
    public List<NotificationPreference> getPreferences(Long userId) {
        return preferenceRepository.findByUserId(userId);
    }

    /**
     * Creates or updates a notification preference for a user.
     * If a preference already exists for the type + channel combination, it is updated.
     * Otherwise, a new preference entry is created.
     *
     * @param userId  the authenticated user ID
     * @param request the preference update payload
     * @return the saved preference
     */
    @Transactional
    public NotificationPreference upsertPreference(Long userId, NotificationPreferenceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        NotificationPreference preference = preferenceRepository
                .findByUserIdAndNotificationTypeAndChannel(
                        userId,
                        request.getNotificationType(),
                        request.getChannel())
                .orElseGet(() -> NotificationPreference.builder()
                        .user(user)
                        .notificationType(request.getNotificationType())
                        .channel(request.getChannel())
                        .build());

        preference.setEnabled(request.getEnabled());
        return preferenceRepository.save(preference);
    }
}
