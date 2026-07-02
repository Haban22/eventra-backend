package com.eventra.backend.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OtpVerifyRequest(
        String pre_auth_token,  // required for admin OTP
        @NotBlank String otp
) {}
