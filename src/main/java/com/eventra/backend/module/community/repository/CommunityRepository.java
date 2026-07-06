package com.eventra.backend.module.community.repository;

import com.eventra.backend.module.community.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    Optional<Community> findByIdAndActiveTrue(Long id);

    List<Community> findAllByActiveTrueOrderByMemberCountDesc();

    List<Community> findAllByActiveTrueOrderByEventCountDesc();

    List<Community> findAllByActiveTrueOrderByNameAsc();

    List<Community> findAllByCategoryAndActiveTrueOrderByMemberCountDesc(String category);

    @Query("SELECT c FROM Community c WHERE c.active = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(c.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Community> searchByNameOrDescription(@Param("q") String query);

    @Query("SELECT c FROM Community c WHERE c.active = true AND " +
           "c.category = :category AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(c.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Community> searchByNameOrDescriptionAndCategory(@Param("q") String query,
                                                          @Param("category") String category);

    boolean existsByNameIgnoreCase(String name);

    List<Community> findAllByCreatedByUserId(java.util.UUID createdByUserId);

    List<Community> findAllByActiveFalseOrderByCreatedAtDesc();
}
