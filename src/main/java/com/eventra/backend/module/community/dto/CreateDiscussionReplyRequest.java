package com.eventra.backend.module.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDiscussionReplyRequest {
    @NotNull
    private Long authorId;

    @NotBlank
    private String authorName;

    private String authorAvatar;

    @NotBlank
    private String content;
}
