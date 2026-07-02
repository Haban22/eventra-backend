package com.eventra.backend.module.notification.controller;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @PersistenceContext
    private EntityManager em;

    @GetMapping("/my")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> myNotifications(@AuthenticationPrincipal AuthPrincipal principal) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT id, type, title, message, is_read, data, created_at FROM notifications WHERE user_id = :uid ORDER BY created_at DESC LIMIT 50")
                .setParameter("uid", principal.userId())
                .getResultList();

        return rows.stream().map(r -> {
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("id",         r[0]);
            n.put("type",       r[1]);
            n.put("title",      r[2]);
            n.put("message",    r[3]);
            n.put("is_read",    r[4]);
            n.put("data",       r[5]);
            n.put("created_at", r[6]);
            return n;
        }).toList();
    }

    @PatchMapping("/{id}/read")
    @Transactional
    public Map<String, Object> markRead(@AuthenticationPrincipal AuthPrincipal principal,
                                        @PathVariable UUID id) {
        em.createNativeQuery("UPDATE notifications SET is_read = true WHERE id = :id AND user_id = :uid")
                .setParameter("id",  id)
                .setParameter("uid", principal.userId())
                .executeUpdate();
        return Map.of("message", "Marked as read");
    }

    @PatchMapping("/read-all")
    @Transactional
    public Map<String, Object> markAllRead(@AuthenticationPrincipal AuthPrincipal principal) {
        em.createNativeQuery("UPDATE notifications SET is_read = true WHERE user_id = :uid")
                .setParameter("uid", principal.userId())
                .executeUpdate();
        return Map.of("message", "All marked as read");
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, Object> delete(@AuthenticationPrincipal AuthPrincipal principal,
                                      @PathVariable UUID id) {
        em.createNativeQuery("DELETE FROM notifications WHERE id = :id AND user_id = :uid")
                .setParameter("id",  id)
                .setParameter("uid", principal.userId())
                .executeUpdate();
        return Map.of("message", "Notification deleted");
    }
}
