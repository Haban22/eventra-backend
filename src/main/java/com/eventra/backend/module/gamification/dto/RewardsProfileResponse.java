package com.eventra.backend.module.gamification.dto;

import com.eventra.backend.module.gamification.valueobject.Level;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class RewardsProfileResponse {
    private UUID userId;
    private String displayName;
    private String avatarUrl;
    private long pointsBalance;
    private long totalXP;
    private long totalPointsEarned;
    private long totalPointsRedeemed;
    private Level level;
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastActivityDate;
    private List<BadgeResponse> earnedBadges;
    private List<BadgeResponse> lockedBadges;
    private List<PointsTransactionResponse> recentTransactions;
}
