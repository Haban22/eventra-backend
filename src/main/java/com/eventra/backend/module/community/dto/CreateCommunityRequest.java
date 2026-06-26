package com.eventra.backend.module.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommunityRequest {
    @NotBlank
    private String name;

    private String description;

    private String coverImage;

    @NotBlank
    private String category;

    private Long createdByUserId;
}
