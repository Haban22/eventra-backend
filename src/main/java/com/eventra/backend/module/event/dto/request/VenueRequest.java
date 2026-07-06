package com.eventra.backend.module.event.dto.request;

import com.eventra.backend.module.event.valueobject.Amenity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VenueRequest(
        @NotBlank String name,
        String address,
        String city,
        Double latitude,
        Double longitude,
        @NotNull @Min(1) int maxCapacity,
        List<Amenity> amenities
) {}