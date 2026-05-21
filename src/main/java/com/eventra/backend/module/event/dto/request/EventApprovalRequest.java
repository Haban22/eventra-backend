package com.eventra.backend.module.event.dto.request;

import com.eventra.backend.module.event.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;

public record EventApprovalRequest(
        @NotNull ApprovalStatus status,
        String feedback
) {}