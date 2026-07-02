package com.eventra.backend.module.event.repository;

import com.eventra.backend.module.event.entity.Event;
import com.eventra.backend.module.event.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

        List<Event> findByOrganizerId(UUID organizerId);

         List<Event> findByStatus(EventStatus status);

         List<Event> findByStatusAndDateTimeAfter(EventStatus status, Instant after);

          List<Event> findByCategoryId(UUID categoryId);

        @Query("""
                SELECT e FROM Event e
                WHERE e.status = 'PUBLISHED'
                AND (CAST(:categoryId AS uuid) IS NULL OR e.category.id = :categoryId)
                AND (CAST(:city AS string) IS NULL OR e.location.city ILIKE %:city%)
                AND (CAST(:keyword AS string) IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
                AND (CAST(:from AS timestamp) IS NULL OR e.dateTime >= :from)
                AND (CAST(:to AS timestamp) IS NULL OR e.dateTime <= :to)
                """)
        Page<Event> searchEvents(
                @Param("categoryId") UUID categoryId,
                @Param("city") String city,
                @Param("keyword") String keyword,
                @Param("from") Instant from,
                @Param("to") Instant to,
                Pageable pageable
        );

        boolean existsByIdAndOrganizerId(UUID id, UUID organizerId);

        long countByStatus(EventStatus status);
}