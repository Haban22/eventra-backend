package com.eventra.backend.module.gamification.entity;

import com.eventra.backend.module.gamification.enums.BadgeCategory;
import com.eventra.backend.module.gamification.enums.BadgeTier;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Badge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeTier tier;

    @Column(nullable = false)
    private String unlockCondition;

    @Column(nullable = false)
    private Integer xpBonus;

    private String iconUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
