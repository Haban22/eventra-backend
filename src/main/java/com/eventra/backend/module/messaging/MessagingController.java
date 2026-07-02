package com.eventra.backend.module.messaging;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class MessagingController {

    @PersistenceContext
    private EntityManager em;

    // ── Direct messages ───────────────────────────────────────────────────────

    @PostMapping("/api/messages/direct")
    @Transactional
    public Map<String, Object> sendDm(@AuthenticationPrincipal AuthPrincipal principal,
                                      @RequestBody Map<String, Object> body) {
        UUID id = UUID.randomUUID();
        em.createNativeQuery(
                "INSERT INTO direct_messages (id, sender_id, receiver_id, content) VALUES (:id, :from, :to, :content)")
                .setParameter("id",      id)
                .setParameter("from",    principal.userId())
                .setParameter("to",      UUID.fromString(body.get("receiverId").toString()))
                .setParameter("content", body.get("content"))
                .executeUpdate();
        return Map.of("id", id, "message", "Message sent");
    }

    @GetMapping("/api/messages/direct/{userId}")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getConversation(@AuthenticationPrincipal AuthPrincipal principal,
                                                     @PathVariable UUID userId) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT id, sender_id, receiver_id, content, is_read, created_at FROM direct_messages " +
                "WHERE (sender_id = :me AND receiver_id = :other) OR (sender_id = :other AND receiver_id = :me) " +
                "ORDER BY created_at ASC")
                .setParameter("me",    principal.userId())
                .setParameter("other", userId)
                .getResultList();
        return toMessageList(rows);
    }

    @GetMapping("/api/messages/threads")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getThreads(@AuthenticationPrincipal AuthPrincipal principal) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT DISTINCT ON (other_user) other_user, id, content, created_at FROM (" +
                "  SELECT CASE WHEN sender_id = :me THEN receiver_id ELSE sender_id END as other_user, id, content, created_at " +
                "  FROM direct_messages WHERE sender_id = :me OR receiver_id = :me ORDER BY created_at DESC" +
                ") t ORDER BY other_user, created_at DESC")
                .setParameter("me", principal.userId())
                .getResultList();
        return rows.stream().map(r -> Map.of(
                "other_user_id", r[0],
                "last_message_id", r[1],
                "last_message", r[2],
                "last_at", r[3]
        )).toList();
    }

    @PatchMapping("/api/messages/direct/{userId}/read")
    @Transactional
    public Map<String, Object> markRead(@AuthenticationPrincipal AuthPrincipal principal,
                                        @PathVariable UUID userId) {
        em.createNativeQuery("UPDATE direct_messages SET is_read = true WHERE sender_id = :other AND receiver_id = :me")
                .setParameter("other", userId)
                .setParameter("me",    principal.userId())
                .executeUpdate();
        return Map.of("message", "Marked as read");
    }

    // ── Broadcasts ────────────────────────────────────────────────────────────

    @PostMapping("/api/broadcasts")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Map<String, Object> sendBroadcast(@AuthenticationPrincipal AuthPrincipal principal,
                                             @RequestBody Map<String, Object> body) {
        UUID id = UUID.randomUUID();
        em.createNativeQuery(
                "INSERT INTO broadcasts (id, sender_id, subject, content, target_role) VALUES (:id, :uid, :sub, :content, :role)")
                .setParameter("id",      id)
                .setParameter("uid",     principal.userId())
                .setParameter("sub",     body.get("subject"))
                .setParameter("content", body.get("content"))
                .setParameter("role",    body.getOrDefault("targetRole", "ATTENDEE"))
                .executeUpdate();
        return Map.of("id", id, "message", "Broadcast sent");
    }

    @GetMapping("/api/broadcasts")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getBroadcasts(@AuthenticationPrincipal AuthPrincipal principal) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT id, subject, content, target_role, created_at FROM broadcasts WHERE sender_id = :uid ORDER BY created_at DESC")
                .setParameter("uid", principal.userId())
                .getResultList();
        return rows.stream().map(r -> Map.of(
                "id", r[0], "subject", r[1], "content", r[2], "target_role", r[3], "created_at", r[4]
        )).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<Map<String, Object>> toMessageList(List<Object[]> rows) {
        return rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",          r[0]);
            m.put("sender_id",   r[1]);
            m.put("receiver_id", r[2]);
            m.put("content",     r[3]);
            m.put("is_read",     r[4]);
            m.put("created_at",  r[5]);
            return m;
        }).toList();
    }
}
