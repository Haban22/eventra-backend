package com.eventra.backend.module.gamification.entity;

import com.eventra.backend.module.gamification.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class PointsTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long xpAmount = 0L;

    @Column(nullable = false)
    private Long pointsAmount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private String reason;

    private String referenceId;

    private String description;

    @Column(nullable = false)
    private Long pointsBalanceAfter;

    @Column(nullable = false)
    private Long xpBalanceAfter;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.xpAmount == null) this.xpAmount = 0L;
        if (this.pointsAmount == null) this.pointsAmount = 0L;
    }
}
