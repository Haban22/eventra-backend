package com.eventra.backend.module.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @JsonProperty("current_password")
        @NotBlank
        String currentPassword,

        @JsonProperty("new_password")
        @NotBlank @Size(min = 8, max = 128)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "must contain at least one uppercase letter, one lowercase letter, and one digit")
        String newPassword,

        @JsonProperty("confirm_password")
        @NotBlank
        String confirmPassword
) {
}
