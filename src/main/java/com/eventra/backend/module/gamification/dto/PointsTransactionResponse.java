package com.eventra.backend.module.gamification.dto;

import com.eventra.backend.module.gamification.enums.TransactionType;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PointsTransactionResponse {
    private Long id;
    private UUID userId;
    private long xpAmount;
    private long pointsAmount;
    private TransactionType type;
    private String reason;
    private String referenceId;
    private String description;
    private long pointsBalanceAfter;
    private long xpBalanceAfter;
    private LocalDateTime createdAt;
}
