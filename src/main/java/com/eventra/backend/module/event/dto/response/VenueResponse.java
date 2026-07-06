package com.eventra.backend.module.event.dto.response;

import com.eventra.backend.module.event.entity.Venue;
import com.eventra.backend.module.event.valueobject.Amenity;

import java.util.List;
import java.util.UUID;

public record VenueResponse(
        UUID id,
        String name,
        String address,
        String city,
        Double latitude,
        Double longitude,
        int maxCapacity,
        List<Amenity> amenities
) {
    public static VenueResponse from(Venue v) {
        return new VenueResponse(
                v.getId(),
                v.getName(),
                v.getLocation() != null ? v.getLocation().getAddress() : null,
                v.getLocation() != null ? v.getLocation().getCity() : null,
                v.getLocation() != null ? v.getLocation().getLatitude() : null,
                v.getLocation() != null ? v.getLocation().getLongitude() : null,
                v.getMaxCapacity(),
                v.getAmenities()
        );
    }
}