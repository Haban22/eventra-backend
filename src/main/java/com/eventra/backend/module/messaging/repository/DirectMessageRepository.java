package com.eventra.backend.module.messaging.repository;

import com.eventra.backend.module.messaging.entity.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

    @Query("SELECT m FROM DirectMessage m WHERE " +
            "(m.senderId = :userA AND m.receiverId = :userB) OR (m.senderId = :userB AND m.receiverId = :userA) " +
            "ORDER BY m.createdAt ASC")
    List<DirectMessage> findConversation(@Param("userA") UUID userA, @Param("userB") UUID userB);

    @Query("SELECT m FROM DirectMessage m WHERE m.senderId = :me OR m.receiverId = :me ORDER BY m.createdAt DESC")
    List<DirectMessage> findAllForUser(@Param("me") UUID me);

    @Modifying
    @Query("UPDATE DirectMessage m SET m.isRead = true WHERE m.receiverId = :me AND m.senderId = :other AND m.isRead = false")
    void markConversationRead(@Param("me") UUID me, @Param("other") UUID other);
}
