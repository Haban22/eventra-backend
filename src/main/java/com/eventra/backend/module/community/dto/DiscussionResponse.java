package com.eventra.backend.module.community.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DiscussionResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
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
