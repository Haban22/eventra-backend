package com.eventra.backend.module.auth.dto.response;

import com.eventra.backend.module.auth.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UserResponse(
        UUID id,
        @JsonProperty("full_name") String fullName,
        String email,
        String phone,
        String role,
        String status,
        @JsonProperty("profile_picture_url") String profilePictureUrl,
        @JsonProperty("language_preference") String languagePreference,
        @JsonProperty("notification_preferences") Map<String, Object> notificationPreferences,
        @JsonProperty("created_at") Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getProfilePictureUrl(),
                user.getLanguagePreference(),
                user.getNotificationPreferences(),
                user.getCreatedAt()
        );
    }
}
