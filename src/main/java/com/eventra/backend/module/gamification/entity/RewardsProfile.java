package com.eventra.backend.module.gamification.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "rewards_profile", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class RewardsProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private String displayName;

    private String avatarUrl;

    @Column(nullable = false)
    private Long pointsBalance = 0L;

    @Column(nullable = false)
    private Long totalPointsEarned = 0L;

    @Column(nullable = false)
    private Long totalPointsRedeemed = 0L;

    @Column(nullable = false)
    private Long totalXP = 0L;

    @Column(nullable = false)
    private Integer currentLevel = 1;

    @Column(nullable = false)
    private Integer currentStreak = 0;

    @Column(nullable = false)
    private Integer longestStreak = 0;

    private LocalDate lastActivityDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.pointsBalance == null) this.pointsBalance = 0L;
        if (this.totalPointsEarned == null) this.totalPointsEarned = 0L;
        if (this.totalPointsRedeemed == null) this.totalPointsRedeemed = 0L;
        if (this.totalXP == null) this.totalXP = 0L;
        if (this.currentLevel == null) this.currentLevel = 1;
        if (this.currentStreak == null) this.currentStreak = 0;
        if (this.longestStreak == null) this.longestStreak = 0;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
