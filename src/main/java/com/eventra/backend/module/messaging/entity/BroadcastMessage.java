package com.eventra.backend.module.messaging.entity;

import com.eventra.backend.module.messaging.enums.BroadcastTargetRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "broadcast_messages")
@Getter
@Setter
public class BroadcastMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "target_role", nullable = false, columnDefinition = "broadcast_target_role")
    private BroadcastTargetRole targetRole;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
