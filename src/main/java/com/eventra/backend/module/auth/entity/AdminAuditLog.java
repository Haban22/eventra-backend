package com.eventra.backend.module.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLInetJdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "admin_audit_logs")
public class AdminAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "admin_user_id", nullable = false)
    private UUID adminUserId;

    @Column(name = "target_user_id", nullable = false)
    private UUID targetUserId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "previous_status", columnDefinition = "user_status")
    private UserStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "new_status", columnDefinition = "user_status")
    private UserStatus newStatus;

    @Column(name = "action_reason")
    private String actionReason;

    @JdbcType(PostgreSQLInetJdbcType.class)
    @Column(name = "ip_address")
    private InetAddress ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
