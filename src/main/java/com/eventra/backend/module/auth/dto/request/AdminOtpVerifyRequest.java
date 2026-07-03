package com.eventra.backend.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminOtpVerifyRequest(
    @NotBlank String preAuthToken,
    @NotBlank String code
) {}
