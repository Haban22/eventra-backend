package com.eventra.backend.module.gamification.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class RedeemPointsRequest {
    @NotNull
    private UUID userId;

    @NotNull
    @Min(1)
    private Long cost;

    private String description;
}
