package com.eventra.backend.module.auth.dto.response;

import com.eventra.auth.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        @JsonProperty("full_name") String fullName,
        String email,
        String role,
        String status
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), user.getStatus().name());
    }
}
