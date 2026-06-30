package com.eventra.backend.module.community.dto;

import com.eventra.backend.module.community.enums.ContentType;
import com.eventra.backend.module.community.enums.FlagStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FlaggedContentResponse {
    private Long id;
    private ContentType contentType;
    private Long contentId;
    private Long communityId;
    private UUID authorId;
    private String authorName;
    private String contentPreview;
    private String reason;
    private Integer reportCount;
    private FlagStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
