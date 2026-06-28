package com.eventra.backend.module.notification.entity;

import com.eventra.backend.module.notification.enums.NotificationChannel;
import com.eventra.backend.module.notification.enums.NotificationType;
import com.eventra.backend.module.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores per-user notification preferences: which channel to use for which notification type.
 * A user can configure each NotificationType independently per NotificationChannel.
 */
@Entity
@Table(
    name = "notification_preferences",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_notification_pref_user_type_channel",
            columnNames = {"user_id", "notification_type", "channel"}
        )
    },
    indexes = {
        @Index(name = "idx_notification_pref_user_id", columnList = "user_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    /**
     * Whether this channel is enabled for this notification type.
     * Defaults to true — opt-out model.
     */
    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
