package com.eventra.backend.module.wallet.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectPayoutRequest(
        @NotBlank String reason
) {}
