package com.eventra.backend.module.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(@JsonProperty("id_token") @NotBlank String idToken) {
}
