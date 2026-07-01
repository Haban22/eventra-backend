package com.eventra.backend.module.wallet.dto;

import com.eventra.backend.module.wallet.entity.WalletTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletTransactionResponse(
        UUID id,
        UUID userId,
        String type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        String referenceId,
        Instant createdAt
) {
    public static WalletTransactionResponse from(WalletTransaction t) {
        return new WalletTransactionResponse(
                t.getId(), t.getUserId(), t.getType().name(), t.getAmount(),
                t.getBalanceAfter(), t.getDescription(), t.getReferenceId(), t.getCreatedAt()
        );
    }
}
