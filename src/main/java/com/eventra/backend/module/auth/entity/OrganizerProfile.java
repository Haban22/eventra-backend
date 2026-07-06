package com.eventra.backend.module.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "organizer_profiles")
public class OrganizerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @Column(name = "organization_description", nullable = false)
    private String organizationDescription;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "social_link")
    private String socialLink;

    @Column(name = "experience")
    private String experience;

    @Column(name = "identity_type")
    private String identityType;

    @Column(name = "team_size")
    private String teamSize;

    @Column(name = "tagline")
    private String tagline;

    @Column(name = "brand_color")
    private String brandColor;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "email_verified_org", nullable = false)
    private boolean emailVerifiedOrg = false;

    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;

    @Column(name = "id_verified", nullable = false)
    private boolean idVerified = false;

    @ElementCollection
    @CollectionTable(name = "organizer_event_types", joinColumns = @JoinColumn(name = "organizer_profile_id"))
    @Column(name = "event_type")
    private java.util.List<String> eventTypes = new java.util.ArrayList<>();

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
