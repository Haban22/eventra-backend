package com.eventra.backend.module.event.dto.response;

import com.eventra.backend.module.event.entity.EventApproval;
import com.eventra.backend.module.event.enums.ApprovalStatus;

import java.time.Instant;
import java.util.UUID;

public record EventApprovalResponse(
        UUID id,
        UUID eventId,
        UUID adminId,
        ApprovalStatus status,
        String feedback,
        Instant reviewedAt,
        Instant createdAt
) {
    public static EventApprovalResponse from(EventApproval a) {
        return new EventApprovalResponse(
                a.getId(),
                a.getEventId(),
                a.getAdminId(),
                a.getStatus(),
                a.getFeedback(),
                a.getReviewedAt(),
                a.getCreatedAt()
        );
    }
}