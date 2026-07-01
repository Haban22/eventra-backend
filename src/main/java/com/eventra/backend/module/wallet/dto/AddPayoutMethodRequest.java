package com.eventra.backend.module.wallet.dto;

import com.eventra.backend.module.wallet.enums.PayoutMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddPayoutMethodRequest(
        @NotNull PayoutMethodType type,
        @NotBlank String accountName,
        @NotBlank String accountNumber,
        String bankName,
        String phone,
        boolean isDefault
) {}
