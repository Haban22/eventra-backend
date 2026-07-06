package com.eventra.backend.module.wallet.controller;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.wallet.dto.PayoutRequestResponse;
import com.eventra.backend.module.wallet.dto.RejectPayoutRequest;
import com.eventra.backend.module.wallet.enums.PayoutStatus;
import com.eventra.backend.module.wallet.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/wallet")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWalletController {
    private final WalletService walletService;

    public AdminWalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/payout-requests")
    public Page<PayoutRequestResponse> getPayoutRequests(
            @RequestParam(required = false) PayoutStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return walletService.getAllPayoutRequests(status, page, size);
    }

    @PatchMapping("/payout-requests/{id}/approve")
    public PayoutRequestResponse approve(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id, HttpServletRequest request) {
        return walletService.approvePayoutRequest(principal.userId(), id, null, request.getRemoteAddr());
    }

    @PatchMapping("/payout-requests/{id}/reject")
    public PayoutRequestResponse reject(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id, @Valid @RequestBody RejectPayoutRequest request, HttpServletRequest httpRequest) {
        return walletService.rejectPayoutRequest(principal.userId(), id, request.reason(), httpRequest.getRemoteAddr());
    }
}
