package com.eventra.backend.module.community.repository;

import com.eventra.backend.module.community.entity.CommunityMember;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommunityMemberRepository extends JpaRepository<CommunityMember, Long> {

    boolean existsByCommunityIdAndUserId(Long communityId, Long userId);

    Optional<CommunityMember> findByCommunityIdAndUserId(Long communityId, Long userId);

    List<CommunityMember> findByCommunityIdOrderByJoinedAtAsc(Long communityId);

    List<CommunityMember> findByCommunityIdOrderByJoinedAtAsc(Long communityId, Pageable pageable);

    long countByCommunityId(Long communityId);

    List<CommunityMember> findByUserId(Long userId);
}
