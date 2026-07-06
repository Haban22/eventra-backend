package com.eventra.backend.module.event.entity;

import com.eventra.backend.module.event.enums.EventStatus;
import com.eventra.backend.module.event.valueobject.Capacity;
import com.eventra.backend.module.event.valueobject.Location;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Plain UUID — no JPA cross-module relationship
    @Column(name = "organizer_id", nullable = false)
    private UUID organizerId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_datetime", nullable = false)
    private Instant dateTime;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "address",  column = @Column(name = "location_address")),
        @AttributeOverride(name = "city",     column = @Column(name = "location_city")),
        @AttributeOverride(name = "latitude", column = @Column(name = "location_latitude")),
        @AttributeOverride(name = "longitude",column = @Column(name = "location_longitude"))
    })
    private Location location;

    // Optional venue reference
    @Column(name = "venue_id")
    private UUID venueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "maximum",  column = @Column(name = "capacity_maximum")),
        @AttributeOverride(name = "reserved", column = @Column(name = "capacity_reserved"))
    })
    private Capacity capacity;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "event_status")
    private EventStatus status = EventStatus.DRAFT;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "is_online", nullable = false)
    private boolean isOnline;

    @Column(name = "online_url")
    private String onlineUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ElementCollection
    @CollectionTable(name = "event_tags", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "tag", length = 50)
    private List<String> tags = new ArrayList<>();

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void publish() {
        this.status = EventStatus.PENDING_APPROVAL;
    }

    public void cancel() {
        this.status = EventStatus.CANCELLED;
    }

    public void approve() {
        this.status = EventStatus.PUBLISHED;
    }
}