package com.eventra.backend.module.community.dto;

import com.eventra.backend.module.community.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FlagContentRequest {
    @NotNull
    private ContentType contentType;

    @NotNull
    private Long contentId;

    private Long communityId;

    @NotNull
    private Long authorId;

    @NotBlank
    private String authorName;

    private String contentPreview;

    @NotBlank
    private String reason;
}
