package com.eventra.backend.module.wallet.controller;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.wallet.dto.*;
import com.eventra.backend.module.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me")
    public WalletResponse getMyWallet(@AuthenticationPrincipal AuthPrincipal principal) {
        return walletService.getWalletResponse(principal.userId());
    }

    @PostMapping("/deposit")
    public WalletTransactionResponse deposit(@AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody DepositRequest request) {
        return walletService.deposit(principal.userId(), request.amount());
    }

    @GetMapping("/transactions")
    public Page<WalletTransactionResponse> getTransactions(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return walletService.getTransactions(principal.userId(), page, size);
    }

    @PostMapping("/payout-methods")
    public PayoutMethodResponse addPayoutMethod(@AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody AddPayoutMethodRequest request) {
        return walletService.addPayoutMethod(principal.userId(), request);
    }

    @DeleteMapping("/payout-methods/{id}")
    public void removePayoutMethod(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        walletService.removePayoutMethod(principal.userId(), id);
    }

    @PostMapping("/payout-requests")
    public PayoutRequestResponse requestPayout(@AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody CreatePayoutRequest request) {
        return walletService.requestPayout(principal.userId(), request);
    }

    @GetMapping("/payout-requests")
    public Page<PayoutRequestResponse> getMyPayoutRequests(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return walletService.getMyPayoutRequests(principal.userId(), page, size);
    }
}
