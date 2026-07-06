package com.eventra.backend.module.community.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunityResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String name;
    private String description;
    private String coverImage;
    private String category;
    private Long memberCount;
    private Long eventCount;
    private boolean joined;
    private boolean active;
    private LocalDateTime createdAt;
    private String createdByUserId;
}
