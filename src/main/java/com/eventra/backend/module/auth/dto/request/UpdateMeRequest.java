package com.eventra.backend.module.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record UpdateMeRequest(
        @JsonProperty("full_name")
        @Size(min = 2, max = 100)
        String fullName,

        @Pattern(regexp = "^$|^\\+?[0-9][0-9\\s().-]{7,29}$", message = "must be a valid phone number")
        String phone,

        @JsonProperty("profile_picture_url")
        @Pattern(regexp = "^$|^https?://.+$", message = "must be a valid URL")
        String profilePictureUrl,

        @JsonProperty("language_preference")
        @Pattern(regexp = "^(en|ar)$")
        String languagePreference,

        @JsonProperty("notification_preferences")
        Map<String, Object> notificationPreferences
) {
}
