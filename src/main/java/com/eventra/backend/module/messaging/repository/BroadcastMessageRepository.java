package com.eventra.backend.module.messaging.repository;

import com.eventra.backend.module.messaging.entity.BroadcastMessage;
import com.eventra.backend.module.messaging.enums.BroadcastTargetRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BroadcastMessageRepository extends JpaRepository<BroadcastMessage, UUID> {
    List<BroadcastMessage> findBySenderIdOrTargetRoleOrderByCreatedAtDesc(UUID senderId, BroadcastTargetRole targetRole);

    @Query("""
        SELECT b FROM BroadcastMessage b
        WHERE b.targetRole = 'ATTENDEE'
        AND (
            b.eventId IS NULL
            OR b.eventId IN (
                SELECT bk.eventId FROM Booking bk
                WHERE bk.attendeeId = :attendeeId
                AND bk.status = 'CONFIRMED'
            )
        )
        ORDER BY b.createdAt DESC
    """)
    List<BroadcastMessage> findAttendeeBroadcasts(@Param("attendeeId") UUID attendeeId);
}
