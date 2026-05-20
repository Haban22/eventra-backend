package com.eventra.backend.module.auth.dto.response;

import com.eventra.backend.module.auth.entity.OrganizerProfile;
import com.eventra.backend.module.auth.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record OrganizerDetailResponse(
        @JsonProperty("user_id") UUID userId,
        @JsonProperty("full_name") String fullName,
        String email,
        String phone,
        String status,
        @JsonProperty("profile_picture_url") String profilePictureUrl,
        @JsonProperty("organization_name") String organizationName,
        @JsonProperty("organization_description") String organizationDescription,
        @JsonProperty("website_url") String websiteUrl,
        @JsonProperty("social_link") String socialLink,
        @JsonProperty("registration_date") Instant registrationDate
) {
    public static OrganizerDetailResponse from(User user, OrganizerProfile profile) {
        return new OrganizerDetailResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), user.getStatus().name(),
                user.getProfilePictureUrl(), profile.getOrganizationName(), profile.getOrganizationDescription(),
                profile.getWebsiteUrl(), profile.getSocialLink(), user.getCreatedAt());
    }
}
