package com.eventra.backend.module.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.util.List;

public record OrganizerRegistrationRequest(
        @JsonProperty("full_name")
        @NotBlank @Size(min = 2, max = 100)
        String fullName,

        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8, max = 128)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "must contain at least one uppercase letter, one lowercase letter, and one digit")
        String password,

        @NotBlank
        @Pattern(regexp = "^\\+?[0-9][0-9\\s().-]{7,29}$",
                message = "must be a valid phone number")
        String phone,

        String city,

        @JsonProperty("organization_name")
        String organizationName,

        @JsonProperty("organization_description")
        String organizationDescription,

        @JsonProperty("website_url")
        String websiteUrl,

        @JsonProperty("social_link")
        String socialLink,

        @JsonProperty("profile_picture_url")
        String profilePictureUrl,

        String experience,

        @JsonProperty("event_types")
        List<String> eventTypes
) {}