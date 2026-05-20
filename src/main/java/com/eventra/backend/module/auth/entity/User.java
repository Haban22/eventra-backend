package com.eventra.backend.module.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    private String phone;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "user_role")
    private UserRole role = UserRole.ATTENDEE;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "user_status")
    private UserStatus status = UserStatus.PENDING_EMAIL_VERIFICATION;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "language_preference", nullable = false)
    private String languagePreference = "en";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notification_preferences", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> notificationPreferences = new HashMap<>();

    @Column(name = "failed_login_attempts", nullable = false)
    private short failedLoginAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        email = email == null ? null : email.toLowerCase();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
        email = email == null ? null : email.toLowerCase();
    }
}
