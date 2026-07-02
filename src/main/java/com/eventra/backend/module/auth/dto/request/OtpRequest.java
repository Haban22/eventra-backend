package com.eventra.backend.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OtpRequest(
        @NotBlank String pre_auth_token
) {}
