package com.eventra.backend.module.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OrganizerRegistrationRequest(
        @JsonProperty("full_name")
        @NotBlank @Size(min = 2, max = 100)
        String fullName,

        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8, max = 128)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "must contain at least one uppercase letter, one lowercase letter, and one digit")
        String password,

        @NotBlank
        @Pattern(regexp = "^\\+?[0-9][0-9\\s().-]{7,29}$", message = "must be a valid phone number")
        String phone,

        @JsonProperty("organization_name")
        @NotBlank @Size(max = 200)
        String organizationName,

        @JsonProperty("organization_description")
        @NotBlank @Size(max = 2000)
        String organizationDescription,

        @JsonProperty("website_url")
        @Pattern(regexp = "^$|^https?://.+$", message = "must be a valid URL")
        String websiteUrl,

        @JsonProperty("social_link")
        @Pattern(regexp = "^$|^https?://.+$", message = "must be a valid URL")
        String socialLink,

        @JsonProperty("profile_picture_url")
        @Pattern(regexp = "^$|^https?://.+$", message = "must be a valid URL")
        String profilePictureUrl
) {
}
