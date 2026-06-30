package com.eventra.backend.module.community.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "community")
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String coverImage;

    @Column(nullable = false)
    private String category;

    private UUID createdByUserId;

    @Column(nullable = false)
    private Long memberCount = 0L;

    @Column(nullable = false)
    private Long eventCount = 0L;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.memberCount == null) this.memberCount = 0L;
        if (this.eventCount == null) this.eventCount = 0L;
        if (this.active == null) this.active = true;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
