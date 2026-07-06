package com.eventra.backend.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OnboardingOtpVerifyRequest(
    @NotBlank String code
) {}
