package com.eventra.backend.module.community.repository;

import com.eventra.backend.module.community.entity.FlaggedContent;
import com.eventra.backend.module.community.enums.ContentType;
import com.eventra.backend.module.community.enums.FlagStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlaggedContentRepository extends JpaRepository<FlaggedContent, Long> {

    List<FlaggedContent> findAllByOrderByCreatedAtDesc();

    List<FlaggedContent> findByStatusOrderByCreatedAtDesc(FlagStatus status);

    Optional<FlaggedContent> findByContentTypeAndContentId(ContentType contentType, Long contentId);

    long countByStatus(FlagStatus status);
}
