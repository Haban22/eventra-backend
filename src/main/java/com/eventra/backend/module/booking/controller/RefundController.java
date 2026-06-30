package com.eventra.backend.module.booking.controller;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.booking.dto.request.PaymentRequest;
import com.eventra.backend.module.booking.dto.request.RefundRequest;
import com.eventra.backend.module.booking.dto.response.PaymentResponse;
import com.eventra.backend.module.booking.dto.response.RefundResponse;
import com.eventra.backend.module.booking.service.PaymentService;
import com.eventra.backend.module.booking.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class RefundController {

    private final RefundService refundService;
    private final PaymentService paymentService;

    public RefundController(RefundService refundService,
                            PaymentService paymentService) {
        this.refundService = refundService;
        this.paymentService = paymentService;
    }

    @PostMapping("/payments/process")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ATTENDEE')")
    public PaymentResponse processPayment(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody PaymentRequest request) {
        return paymentService.processPayment(request);
    }

    @PostMapping("/refunds")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ATTENDEE')")
    public RefundResponse requestRefund(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody RefundRequest request) {
        return refundService.processRefund(principal.userId(), request);
    }
}