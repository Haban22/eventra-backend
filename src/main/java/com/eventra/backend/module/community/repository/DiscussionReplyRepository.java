package com.eventra.backend.module.community.repository;

import com.eventra.backend.module.community.entity.DiscussionReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscussionReplyRepository extends JpaRepository<DiscussionReply, Long> {

    List<DiscussionReply> findByDiscussionIdAndActiveTrueOrderByCreatedAtAsc(Long discussionId);

    long countByDiscussionIdAndActiveTrue(Long discussionId);
}
