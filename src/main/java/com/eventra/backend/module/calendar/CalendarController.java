package com.eventra.backend.module.calendar;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    @PersistenceContext
    private EntityManager em;

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> myCalendar(@AuthenticationPrincipal AuthPrincipal principal) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT id, title, description, date, end_date, location, type, category, created_at FROM calendar_events WHERE user_id = :uid ORDER BY date ASC")
                .setParameter("uid", principal.userId())
                .getResultList();

        return rows.stream().map(r -> {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("id",          r[0]);
            e.put("title",       r[1]);
            e.put("description", r[2]);
            e.put("date",        r[3]);
            e.put("end_date",    r[4]);
            e.put("location",    r[5]);
            e.put("type",        r[6]);
            e.put("category",    r[7]);
            e.put("created_at",  r[8]);
            return e;
        }).toList();
    }

    @PostMapping
    @Transactional
    public Map<String, Object> create(@AuthenticationPrincipal AuthPrincipal principal,
                                      @RequestBody Map<String, Object> body) {
        UUID id = UUID.randomUUID();
        em.createNativeQuery(
                "INSERT INTO calendar_events (id, user_id, title, description, date, end_date, location, type, category) " +
                "VALUES (:id, :uid, :title, :desc, CAST(:date AS timestamptz), CAST(:end AS timestamptz), :loc, :type, :cat)")
                .setParameter("id",    id)
                .setParameter("uid",   principal.userId())
                .setParameter("title", body.get("title"))
                .setParameter("desc",  body.get("description"))
                .setParameter("date",  body.get("date"))
                .setParameter("end",   body.get("endDate"))
                .setParameter("loc",   body.get("location"))
                .setParameter("type",  body.getOrDefault("type", "PERSONAL"))
                .setParameter("cat",   body.get("category"))
                .executeUpdate();
        return Map.of("id", id, "message", "Event created");
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, Object> delete(@AuthenticationPrincipal AuthPrincipal principal,
                                      @PathVariable UUID id) {
        em.createNativeQuery("DELETE FROM calendar_events WHERE id = :id AND user_id = :uid")
                .setParameter("id",  id)
                .setParameter("uid", principal.userId())
                .executeUpdate();
        return Map.of("message", "Event deleted");
    }
}
