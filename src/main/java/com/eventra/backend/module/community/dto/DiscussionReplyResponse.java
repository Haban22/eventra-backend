package com.eventra.backend.module.community.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DiscussionReplyResponse {
    private Long id;
    private Long discussionId;
    private UUID authorId;
    private String authorName;
    private String authorAvatar;
    private String content;
    private LocalDateTime createdAt;
}
