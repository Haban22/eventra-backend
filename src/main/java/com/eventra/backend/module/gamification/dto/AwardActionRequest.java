package com.eventra.backend.module.gamification.dto;

import com.eventra.backend.module.gamification.enums.GamificationAction;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AwardActionRequest {
    @NotNull
    private UUID userId;

    @NotNull
    private GamificationAction action;

    /** Optional: event ID, booking ID, etc. Used for dedup (one reward per reference). */
    private String referenceId;

    /** Display name to use if creating a new RewardsProfile for this user. */
    private String displayName;

    private String avatarUrl;
}
