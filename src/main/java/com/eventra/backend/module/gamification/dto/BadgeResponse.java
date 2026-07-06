package com.eventra.backend.module.gamification.dto;

import com.eventra.backend.module.gamification.enums.BadgeCategory;
import com.eventra.backend.module.gamification.enums.BadgeTier;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BadgeResponse {
    private Long id;
    private String name;
    private String description;
    private BadgeCategory category;
    private BadgeTier tier;
    private String unlockCondition;
    private Integer xpBonus;
    private String iconUrl;
    private boolean unlocked;
    private LocalDateTime unlockedAt;
}
