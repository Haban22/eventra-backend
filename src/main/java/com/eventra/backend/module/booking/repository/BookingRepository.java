package com.eventra.backend.module.booking.repository;

import com.eventra.backend.module.booking.entity.Booking;
import com.eventra.backend.module.booking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Page<Booking> findByAttendeeId(UUID attendeeId, Pageable pageable);

    List<Booking> findByEventId(UUID eventId);

    List<Booking> findByEventIdAndStatus(UUID eventId, BookingStatus status);

    Optional<Booking> findByIdAndAttendeeId(UUID id, UUID attendeeId);

    boolean existsByAttendeeIdAndEventIdAndStatus(UUID attendeeId, UUID eventId, BookingStatus status);

    // Find expired holds to release
    @Query("""
            SELECT b FROM Booking b
            WHERE b.status = 'PENDING_PAYMENT'
            AND b.holdExpiresAt < :now
            """)
    List<Booking> findExpiredHolds(@Param("now") Instant now);

    // Count confirmed bookings for an event
    long countByEventIdAndStatus(UUID eventId, BookingStatus status);
}