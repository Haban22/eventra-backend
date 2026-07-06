package com.eventra.backend.module.booking.gateway;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class StripeGateway {

    // Mock payment — always succeeds in demo
    // Replace with real Stripe SDK call in production
    public String processPayment(BigDecimal amount, String method, String details) {
        // Simulate a transaction ID
        return "txn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public boolean refund(String transactionId, BigDecimal amount) {
        // Mock refund — always succeeds in demo
        return true;
    }
}