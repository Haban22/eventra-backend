package com.eventra.backend.module.auth.dto.response;

import com.eventra.backend.module.auth.entity.OrganizerProfile;
import com.eventra.backend.module.auth.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record OrganizerSummaryResponse(
        @JsonProperty("user_id") UUID userId,
        @JsonProperty("full_name") String fullName,
        String email,
        @JsonProperty("organization_name") String organizationName,
        @JsonProperty("created_at") Instant createdAt,
        String status
) {
    public static OrganizerSummaryResponse from(User user, OrganizerProfile profile) {
        return new OrganizerSummaryResponse(user.getId(), user.getFullName(), user.getEmail(), profile.getOrganizationName(), user.getCreatedAt(), user.getStatus().name());
    }
}
