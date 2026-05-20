package com.eventra.backend.module.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LogoutRequest(@JsonProperty("refresh_token") String refreshToken) {
}
