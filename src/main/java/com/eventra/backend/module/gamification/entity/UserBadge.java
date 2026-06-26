package com.eventra.backend.module.gamification.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "badge_id"}))
public class UserBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    @ToString.Exclude
    private Badge badge;

    @Column(nullable = false, updatable = false)
    private LocalDateTime unlockedAt;

    @Column(nullable = false)
    private Boolean active = true;

    @PrePersist
    void prePersist() {
        this.unlockedAt = LocalDateTime.now();
        if (this.active == null) this.active = true;
    }
}
