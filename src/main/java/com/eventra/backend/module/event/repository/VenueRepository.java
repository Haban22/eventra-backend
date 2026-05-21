package com.eventra.backend.module.event.repository;

import com.eventra.backend.module.event.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {

    Optional<Venue> findByNameIgnoreCase(String name);

    @Query("""
            SELECT v FROM Venue v
            WHERE v.location.city ILIKE %:city%
            ORDER BY v.name ASC
            """)
    List<Venue> findByCity(@Param("city") String city);

    @Query("""
            SELECT v FROM Venue v
            WHERE v.maxCapacity >= :minCapacity
            ORDER BY v.maxCapacity ASC
            """)
    List<Venue> findByMinCapacity(@Param("minCapacity") int minCapacity);
}