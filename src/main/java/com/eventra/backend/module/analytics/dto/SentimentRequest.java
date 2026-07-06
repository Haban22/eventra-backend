package com.eventra.backend.module.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SentimentRequest {

    @NotBlank(message = "text must not be blank")
    private String text;
}
