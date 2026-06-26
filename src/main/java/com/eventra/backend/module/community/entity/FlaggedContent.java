package com.eventra.backend.module.community.entity;

import com.eventra.backend.module.community.enums.ContentType;
import com.eventra.backend.module.community.enums.FlagStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "flagged_content")
public class FlaggedContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType contentType;

    @Column(nullable = false)
    private Long contentId;

    private Long communityId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String authorName;

    @Column(columnDefinition = "TEXT")
    private String contentPreview;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Integer reportCount = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlagStatus status = FlagStatus.PENDING;

    private Long resolvedByUserId;

    private LocalDateTime resolvedAt;

    private String moderatorNote;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.reportCount == null) this.reportCount = 1;
        if (this.status == null) this.status = FlagStatus.PENDING;
    }
}
