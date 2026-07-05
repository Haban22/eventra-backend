package com.eventra.backend.module.auth.dto.response;

import com.eventra.backend.module.auth.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record UserResponse(
        UUID id,
        @JsonProperty("full_name") String fullName,
        String email,
        String phone,
        String role,
        String status,
        String city,
        List<String> interests,
        @JsonProperty("profile_picture_url") String profilePictureUrl,
        @JsonProperty("cover_photo_url") String coverPhotoUrl,
        @JsonProperty("language_preference") String languagePreference,
        @JsonProperty("notification_preferences") Map<String, Object> notificationPreferences,
        @JsonProperty("onboarding_completed") boolean onboardingCompleted,
        @JsonProperty("must_reset_password") boolean mustResetPassword,
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
                user.getCity(),
                user.getInterests(),
                user.getProfilePictureUrl(),
                user.getCoverPhotoUrl(),
                user.getLanguagePreference(),
                user.getNotificationPreferences(),
                user.isOnboardingCompleted(),
                user.isMustResetPassword(),
                user.getCreatedAt()
        );
    }
}