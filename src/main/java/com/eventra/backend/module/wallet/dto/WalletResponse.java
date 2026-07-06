package com.eventra.backend.module.wallet.dto;

import com.eventra.backend.module.wallet.entity.PayoutMethod;
import com.eventra.backend.module.wallet.entity.Wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record WalletResponse(
        UUID userId,
        BigDecimal balance,
        String currency,
        String status,
        List<PayoutMethodResponse> payoutMethods
) {
    public static WalletResponse from(Wallet wallet, List<PayoutMethod> methods) {
        return new WalletResponse(
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getStatus().name(),
                methods.stream().map(PayoutMethodResponse::from).toList()
        );
    }
}
