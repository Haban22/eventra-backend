package com.eventra.backend.module.community.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DiscussionResponse {
    private Long id;
    private Long communityId;
    private String title;
    private String content;
    private UUID authorId;
    private String authorName;
    private String authorAvatar;
    private Integer replyCount;
    private boolean hot;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
