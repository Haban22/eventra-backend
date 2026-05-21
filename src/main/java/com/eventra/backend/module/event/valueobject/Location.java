package com.eventra.backend.module.event.valueobject;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Location {

    private String address;
    private String city;
    private double latitude;
    private double longitude;

    public double distanceTo(Location other) {
        double dx = this.latitude - other.latitude;
        double dy = this.longitude - other.longitude;
        return Math.sqrt(dx * dx + dy * dy);
    }
}