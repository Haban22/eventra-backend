package com.eventra.backend.module.auth.controller;

import com.eventra.backend.module.auth.exception.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/config")
public class SystemConfigController {

    @PersistenceContext
    private EntityManager em;

    @GetMapping
    public Map<String, Object> getConfig() {
        List<Object[]> rows = em.createNativeQuery("SELECT key, value FROM system_config").getResultList();
        Map<String, Object> config = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String key   = (String) row[0];
            String value = (String) row[1];
            // Coerce to appropriate types
            try { config.put(key, Long.parseLong(value)); continue; } catch (NumberFormatException ignored) {}
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                config.put(key, Boolean.parseBoolean(value));
                continue;
            }
            config.put(key, value);
        }
        return config;
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Map<String, Object> updateConfig(@RequestBody Map<String, Object> updates) {
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            int updated = em.createNativeQuery(
                    "UPDATE system_config SET value = :val WHERE key = :key")
                    .setParameter("val", String.valueOf(entry.getValue()))
                    .setParameter("key", entry.getKey())
                    .executeUpdate();
            if (updated == 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "UNKNOWN_CONFIG_KEY",
                        "Unknown config key: " + entry.getKey());
            }
        }
        return getConfig();
    }
}
