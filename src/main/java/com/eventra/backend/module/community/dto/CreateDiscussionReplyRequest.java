package com.eventra.backend.module.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateDiscussionReplyRequest {
    @NotNull
    private UUID authorId;

    @NotBlank
    private String authorName;

    private String authorAvatar;

    @NotBlank
    private String content;
}
