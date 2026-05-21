package com.eventra.backend.module.event.repository;

import com.eventra.backend.module.event.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {

    Optional<Bookmark> findByUserIdAndEventId(UUID userId, UUID eventId);

    Page<Bookmark> findByUserId(UUID userId, Pageable pageable);

    boolean existsByUserIdAndEventId(UUID userId, UUID eventId);

    void deleteByUserIdAndEventId(UUID userId, UUID eventId);
}