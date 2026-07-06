package com.eventra.backend.module.wallet.dto;

import com.eventra.backend.module.wallet.entity.PayoutMethod;
import com.eventra.backend.module.wallet.entity.PayoutRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PayoutRequestResponse(
        UUID id,
        UUID organizerId,
        BigDecimal amount,
        PayoutMethodResponse method,
        String status,
        String notes,
        String adminNotes,
        Instant requestedAt,
        Instant processedAt
) {
    public static PayoutRequestResponse from(PayoutRequest r, PayoutMethod method) {
        return new PayoutRequestResponse(
                r.getId(), r.getOrganizerId(), r.getAmount(),
                method == null ? null : PayoutMethodResponse.from(method),
                r.getStatus().name(), r.getNotes(), r.getAdminNotes(), r.getRequestedAt(), r.getProcessedAt()
        );
    }
}
