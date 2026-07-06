package com.eventra.backend.module.wallet.dto;

import com.eventra.backend.module.wallet.entity.PayoutMethod;

import java.time.Instant;
import java.util.UUID;

public record PayoutMethodResponse(
        UUID id,
        UUID userId,
        String type,
        String accountName,
        String accountNumber,
        String bankName,
        String phone,
        boolean isDefault,
        Instant createdAt
) {
    public static PayoutMethodResponse from(PayoutMethod m) {
        return new PayoutMethodResponse(
                m.getId(), m.getUserId(), m.getType().name(), m.getAccountName(),
                m.getAccountNumber(), m.getBankName(), m.getPhone(), m.isDefault(), m.getCreatedAt()
        );
    }
}
