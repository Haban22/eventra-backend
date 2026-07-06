package com.eventra.backend.module.event.entity;

import com.eventra.backend.module.event.valueobject.Amenity;
import com.eventra.backend.module.event.valueobject.Location;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "address",  column = @Column(name = "address")),
        @AttributeOverride(name = "city",     column = @Column(name = "city")),
        @AttributeOverride(name = "latitude", column = @Column(name = "latitude")),
        @AttributeOverride(name = "longitude",column = @Column(name = "longitude"))
    })
    private Location location;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @ElementCollection
    @CollectionTable(name = "venue_amenities", joinColumns = @JoinColumn(name = "venue_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "amenity", nullable = false)
    private List<Amenity> amenities = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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
}