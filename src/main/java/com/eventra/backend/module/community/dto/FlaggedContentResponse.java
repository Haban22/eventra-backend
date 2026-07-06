package com.eventra.backend.module.community.dto;

import com.eventra.backend.module.community.enums.ContentType;
import com.eventra.backend.module.community.enums.FlagStatus;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FlaggedContentResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private ContentType contentType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long contentId;

    @JsonSerialize(using = ToStringSerializer.class)
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
