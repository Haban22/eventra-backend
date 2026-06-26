package com.eventra.backend.module.community.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaveCommunityRequest {
    @NotNull
    private Long userId;
}
