package com.eventra.backend.module.notification.repository;

import com.eventra.backend.module.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for {@link Notification} entities.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Finds all notifications belonging to a user with pagination.
     * Sorting is delegated entirely to the Pageable parameter to avoid
     * conflicts between inline ORDER BY and Pageable sort specs.
     *
     * @param userId   owner user ID
     * @param pageable pagination and sort parameters
     * @return paginated notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId")
    Page<Notification> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Finds unread notifications for a user with pagination.
     *
     * @param userId   owner user ID
     * @param pageable pagination and sort parameters
     * @return paginated unread notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.read = false")
    Page<Notification> findByUserIdAndIsReadFalse(@Param("userId") Long userId, Pageable pageable);

    /**
     * Finds a notification by ID scoped to a specific user.
     * Used for ownership verification on mutation endpoints.
     *
     * @param id     notification ID
     * @param userId owner user ID
     * @return matching notification if found
     */
    Optional<Notification> findByIdAndUser_Id(Long id, Long userId);

    /**
     * Marks all unread notifications as read for a user atomically.
     * clearAutomatically = true flushes the first-level cache so that
     * subsequent reads within the same transaction reflect the bulk update.
     *
     * @param userId owner user ID
     * @return number of updated rows
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.read = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    /**
     * Returns the count of unread notifications for a user.
     * Used for badge/indicator display without loading full notification objects.
     *
     * @param userId owner user ID
     * @return count of unread notifications
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.read = false")
    long countUnreadByUserId(@Param("userId") Long userId);
}
