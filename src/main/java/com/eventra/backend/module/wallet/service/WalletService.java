package com.eventra.backend.module.wallet.service;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.config.service.SystemConfigService;
import com.eventra.backend.module.notification.service.NotificationService;
import com.eventra.backend.module.wallet.dto.*;
import com.eventra.backend.module.wallet.entity.PayoutMethod;
import com.eventra.backend.module.wallet.entity.PayoutRequest;
import com.eventra.backend.module.wallet.entity.Wallet;
import com.eventra.backend.module.wallet.entity.WalletTransaction;
import com.eventra.backend.module.wallet.enums.PayoutStatus;
import com.eventra.backend.module.wallet.enums.WalletTransactionType;
import com.eventra.backend.module.wallet.repository.PayoutMethodRepository;
import com.eventra.backend.module.wallet.repository.PayoutRequestRepository;
import com.eventra.backend.module.wallet.repository.WalletRepository;
import com.eventra.backend.module.wallet.repository.WalletTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Simulated ledger, no real payment processor — mirrors the existing
// StripeGateway mock pattern (deposits/payouts always "succeed" server-side).
@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final PayoutMethodRepository payoutMethodRepository;
    private final PayoutRequestRepository payoutRequestRepository;
    private final SystemConfigService systemConfigService;
    private final NotificationService notificationService;

    public WalletService(WalletRepository walletRepository,
                         WalletTransactionRepository transactionRepository,
                         PayoutMethodRepository payoutMethodRepository,
                         PayoutRequestRepository payoutRequestRepository,
                         SystemConfigService systemConfigService,
                         NotificationService notificationService) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.payoutMethodRepository = payoutMethodRepository;
        this.payoutRequestRepository = payoutRequestRepository;
        this.systemConfigService = systemConfigService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Wallet getOrCreateWallet(UUID userId) {
        return walletRepository.findByUserId(userId).orElseGet(() -> {
            Wallet wallet = new Wallet();
            wallet.setUserId(userId);
            return walletRepository.save(wallet);
        });
    }

    @Transactional
    public WalletResponse getWalletResponse(UUID userId) {
        Wallet wallet = getOrCreateWallet(userId);
        List<PayoutMethod> methods = payoutMethodRepository.findByUserId(userId);
        return WalletResponse.from(wallet, methods);
    }

    @Transactional
    public WalletTransactionResponse deposit(UUID userId, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);
        return WalletTransactionResponse.from(recordTransaction(userId, WalletTransactionType.DEPOSIT, amount, wallet.getBalance(), "Wallet deposit", null));
    }

    // Debits the wallet for a booking payment. Throws INSUFFICIENT_BALANCE if the
    // wallet can't cover it — caller (PaymentService) should not have already
    // charged anything else, this is the entire payment for a WALLET-method booking.
    @Transactional
    public void payWithWallet(UUID userId, BigDecimal amount, UUID bookingId) {
        Wallet wallet = getOrCreateWallet(userId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE", "Wallet balance is insufficient for this booking");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);
        recordTransaction(userId, WalletTransactionType.PAYMENT, amount.negate(), wallet.getBalance(), "Booking payment", bookingId.toString());
    }

    @Transactional
    public void refundToWallet(UUID userId, BigDecimal amount, UUID bookingId, String reason) {
        Wallet wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);
        recordTransaction(userId, WalletTransactionType.REFUND, amount, wallet.getBalance(),
                reason == null || reason.isBlank() ? "Booking refund" : reason, bookingId.toString());
    }

    // Called from PaymentService after a booking is confirmed — credits the
    // organizer with the booking total minus the platform fee percentage.
    @Transactional
    public void recordOrganizerEarning(UUID organizerId, BigDecimal grossAmount, UUID bookingId) {
        BigDecimal feeRate = systemConfigService.getConfig().getPlatformFeePercentage()
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        BigDecimal fee = grossAmount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal net = grossAmount.subtract(fee);

        Wallet wallet = getOrCreateWallet(organizerId);
        wallet.setBalance(wallet.getBalance().add(net));
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);
        recordTransaction(organizerId, WalletTransactionType.EARNING, net, wallet.getBalance(),
                "Booking earning (platform fee " + systemConfigService.getConfig().getPlatformFeePercentage() + "% deducted)", bookingId.toString());
    }

    @Transactional(readOnly = true)
    public Page<WalletTransactionResponse> getTransactions(UUID userId, int page, int size) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(WalletTransactionResponse::from);
    }

    @Transactional
    public PayoutMethodResponse addPayoutMethod(UUID userId, AddPayoutMethodRequest req) {
        if (req.isDefault()) {
            payoutMethodRepository.findByUserId(userId).forEach(m -> {
                if (m.isDefault()) {
                    m.setDefault(false);
                    payoutMethodRepository.save(m);
                }
            });
        }
        PayoutMethod method = new PayoutMethod();
        method.setUserId(userId);
        method.setType(req.type());
        method.setAccountName(req.accountName());
        method.setAccountNumber(req.accountNumber());
        method.setBankName(req.bankName());
        method.setPhone(req.phone());
        method.setDefault(req.isDefault());
        return PayoutMethodResponse.from(payoutMethodRepository.save(method));
    }

    @Transactional
    public void removePayoutMethod(UUID userId, UUID methodId) {
        PayoutMethod method = payoutMethodRepository.findById(methodId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Payout method not found"));
        if (!method.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_OWNER", "You do not own this payout method");
        }
        payoutMethodRepository.delete(method);
    }

    @Transactional
    public PayoutRequestResponse requestPayout(UUID organizerId, CreatePayoutRequest req) {
        BigDecimal minPayout = systemConfigService.getConfig().getMinPayoutAmount();
        if (req.amount().compareTo(minPayout) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BELOW_MIN_PAYOUT", "Amount is below the minimum payout of " + minPayout);
        }

        PayoutMethod method = payoutMethodRepository.findById(req.methodId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Payout method not found"));
        if (!method.getUserId().equals(organizerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_OWNER", "You do not own this payout method");
        }

        Wallet wallet = getOrCreateWallet(organizerId);
        if (wallet.getBalance().compareTo(req.amount()) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE", "Wallet balance is insufficient for this payout");
        }

        wallet.setBalance(wallet.getBalance().subtract(req.amount()));
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);

        PayoutRequest request = new PayoutRequest();
        request.setOrganizerId(organizerId);
        request.setAmount(req.amount());
        request.setMethodId(method.getId());
        request.setNotes(req.notes());
        request = payoutRequestRepository.save(request);

        recordTransaction(organizerId, WalletTransactionType.PAYOUT, req.amount().negate(), wallet.getBalance(),
                "Payout requested — " + (method.getBankName() != null ? method.getBankName() : method.getType()), request.getId().toString());

        return PayoutRequestResponse.from(request, method);
    }

    @Transactional(readOnly = true)
    public Page<PayoutRequestResponse> getMyPayoutRequests(UUID organizerId, int page, int size) {
        return payoutRequestRepository.findByOrganizerIdOrderByRequestedAtDesc(organizerId, PageRequest.of(page, size))
                .map(r -> PayoutRequestResponse.from(r, payoutMethodRepository.findById(r.getMethodId()).orElse(null)));
    }

    @Transactional(readOnly = true)
    public Page<PayoutRequestResponse> getAllPayoutRequests(PayoutStatus status, int page, int size) {
        Page<PayoutRequest> requests = status == null
                ? payoutRequestRepository.findAllByOrderByRequestedAtDesc(PageRequest.of(page, size))
                : payoutRequestRepository.findByStatusOrderByRequestedAtDesc(status, PageRequest.of(page, size));
        return requests.map(r -> PayoutRequestResponse.from(r, payoutMethodRepository.findById(r.getMethodId()).orElse(null)));
    }

    @Transactional
    public PayoutRequestResponse approvePayoutRequest(UUID requestId, String adminNotes) {
        PayoutRequest request = loadPayoutRequest(requestId);
        if (request.getStatus() != PayoutStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "WRONG_STATUS", "Payout request is not pending");
        }
        request.setStatus(PayoutStatus.APPROVED);
        request.setAdminNotes(adminNotes);
        request.setProcessedAt(Instant.now());
        payoutRequestRepository.save(request);
        notificationService.notify(request.getOrganizerId(), "payout_approved", "Payout Approved! 💰",
                "Your payout request for " + request.getAmount() + " has been approved.", "/organizer/wallet");
        return PayoutRequestResponse.from(request, payoutMethodRepository.findById(request.getMethodId()).orElse(null));
    }

    @Transactional
    public PayoutRequestResponse rejectPayoutRequest(UUID requestId, String reason) {
        PayoutRequest request = loadPayoutRequest(requestId);
        if (request.getStatus() != PayoutStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "WRONG_STATUS", "Payout request is not pending");
        }
        request.setStatus(PayoutStatus.REJECTED);
        request.setAdminNotes(reason);
        request.setProcessedAt(Instant.now());
        payoutRequestRepository.save(request);

        // Refund the held amount back to the organizer's wallet
        Wallet wallet = getOrCreateWallet(request.getOrganizerId());
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);
        recordTransaction(request.getOrganizerId(), WalletTransactionType.REFUND, request.getAmount(), wallet.getBalance(),
                "Payout rejected — funds returned", request.getId().toString());
        notificationService.notify(request.getOrganizerId(), "payout_rejected", "Payout Request Update",
                "Your payout request for " + request.getAmount() + " was not approved." + (reason != null ? " Reason: " + reason : ""), "/organizer/wallet");

        return PayoutRequestResponse.from(request, payoutMethodRepository.findById(request.getMethodId()).orElse(null));
    }

    private PayoutRequest loadPayoutRequest(UUID id) {
        return payoutRequestRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Payout request not found"));
    }

    private WalletTransaction recordTransaction(UUID userId, WalletTransactionType type, BigDecimal amount, BigDecimal balanceAfter, String description, String referenceId) {
        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setBalanceAfter(balanceAfter);
        tx.setDescription(description);
        tx.setReferenceId(referenceId);
        return transactionRepository.save(tx);
    }
}
