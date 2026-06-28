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
     *
     * @param userId   owner user ID
     * @param pageable pagination parameters
     * @return paginated notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    Page<Notification> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Finds unread notifications for a user with pagination.
     *
     * @param userId   owner user ID
     * @param pageable pagination parameters
     * @return paginated unread notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndIsReadFalse(@Param("userId") Long userId, Pageable pageable);

    /**
     * Finds a notification by ID scoped to a specific user.
     *
     * @param id     notification ID
     * @param userId owner user ID
     * @return matching notification if found
     */
    Optional<Notification> findByIdAndUser_Id(Long id, Long userId);

    /**
     * Marks all unread notifications as read for a user.
     *
     * @param userId owner user ID
     * @return number of updated rows
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);
}
