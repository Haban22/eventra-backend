package com.eventra.backend.module.calendar.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "personal_events")
@Getter
@Setter
public class PersonalEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private Instant date;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    private String location;

    @Column(nullable = false)
    private String type;

    private String category;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
