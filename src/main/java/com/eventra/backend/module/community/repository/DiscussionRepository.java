package com.eventra.backend.module.community.repository;

import com.eventra.backend.module.community.entity.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {

    List<Discussion> findByCommunityIdAndActiveTrueOrderByHotDescCreatedAtDesc(Long communityId);

    Optional<Discussion> findByIdAndCommunityIdAndActiveTrue(Long id, Long communityId);

    long countByCommunityIdAndActiveTrue(Long communityId);
}
