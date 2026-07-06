package com.eventra.backend.module.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AdminOtpVerifyRequest(
    @JsonProperty("pre_auth_token") @NotBlank String preAuthToken,
    @JsonProperty("otp") @NotBlank String code
) {}

