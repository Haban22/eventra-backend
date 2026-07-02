package com.eventra.backend.module.wallet;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @PersistenceContext
    private EntityManager em;

    // ── Wallet ────────────────────────────────────────────────────────────────

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public Map<String, Object> getWallet(@AuthenticationPrincipal AuthPrincipal principal) {
        return getOrCreateWallet(principal.userId());
    }

    @PostMapping("/deposit")
    @Transactional
    public Map<String, Object> deposit(@AuthenticationPrincipal AuthPrincipal principal,
                                       @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        Map<String, Object> wallet = getOrCreateWallet(principal.userId());
        UUID walletId = (UUID) wallet.get("id");
        BigDecimal current = (BigDecimal) wallet.get("balance");
        BigDecimal newBalance = current.add(amount);

        em.createNativeQuery("UPDATE wallets SET balance = :bal, updated_at = NOW() WHERE id = :id")
                .setParameter("bal", newBalance)
                .setParameter("id", walletId)
                .executeUpdate();

        em.createNativeQuery("INSERT INTO wallet_transactions (id, wallet_id, type, amount, balance_after, description) VALUES (gen_random_uuid(), :wid, 'DEPOSIT', :amt, :bal, 'Manual deposit')")
                .setParameter("wid", walletId)
                .setParameter("amt", amount)
                .setParameter("bal", newBalance)
                .executeUpdate();

        wallet.put("balance", newBalance);
        return wallet;
    }

    @GetMapping("/transactions")
    @Transactional(readOnly = true)
    public Map<String, Object> transactions(@AuthenticationPrincipal AuthPrincipal principal,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> wallet = getOrCreateWallet(principal.userId());
        UUID walletId = (UUID) wallet.get("id");

        List<Object[]> rows = em.createNativeQuery(
                "SELECT id, type, amount, balance_after, description, created_at FROM wallet_transactions WHERE wallet_id = :wid ORDER BY created_at DESC LIMIT :lim OFFSET :off")
                .setParameter("wid", walletId)
                .setParameter("lim", size)
                .setParameter("off", page * size)
                .getResultList();

        List<Map<String, Object>> items = rows.stream().map(r -> {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("id",            r[0]);
            t.put("type",          r[1]);
            t.put("amount",        r[2]);
            t.put("balance_after", r[3]);
            t.put("description",   r[4]);
            t.put("created_at",    r[5]);
            return t;
        }).toList();

        return Map.of("content", items, "totalElements", items.size(), "page", page, "size", size);
    }

    // ── Payout methods ────────────────────────────────────────────────────────

    @PostMapping("/payout-methods")
    @Transactional
    public Map<String, Object> addPayoutMethod(@AuthenticationPrincipal AuthPrincipal principal,
                                               @RequestBody Map<String, Object> body) {
        UUID id = UUID.randomUUID();
        em.createNativeQuery("INSERT INTO payout_methods (id, user_id, type, account_name, account_number, bank_name, phone, is_default) VALUES (:id, :uid, :type, :name, :num, :bank, :phone, :def)")
                .setParameter("id",    id)
                .setParameter("uid",   principal.userId())
                .setParameter("type",  body.getOrDefault("type", "BANK_TRANSFER"))
                .setParameter("name",  body.getOrDefault("accountName", ""))
                .setParameter("num",   body.getOrDefault("accountNumber", ""))
                .setParameter("bank",  body.get("bankName"))
                .setParameter("phone", body.get("phone"))
                .setParameter("def",   Boolean.TRUE.equals(body.get("isDefault")))
                .executeUpdate();
        return Map.of("id", id, "message", "Payout method added");
    }

    @DeleteMapping("/payout-methods/{id}")
    @Transactional
    public Map<String, Object> removePayoutMethod(@AuthenticationPrincipal AuthPrincipal principal,
                                                  @PathVariable UUID id) {
        em.createNativeQuery("DELETE FROM payout_methods WHERE id = :id AND user_id = :uid")
                .setParameter("id",  id)
                .setParameter("uid", principal.userId())
                .executeUpdate();
        return Map.of("message", "Payout method removed");
    }

    // ── Payout requests ───────────────────────────────────────────────────────

    @PostMapping("/payout-requests")
    @Transactional
    public Map<String, Object> requestPayout(@AuthenticationPrincipal AuthPrincipal principal,
                                             @RequestBody Map<String, Object> body) {
        UUID id = UUID.randomUUID();
        em.createNativeQuery("INSERT INTO payout_requests (id, user_id, method_id, amount, notes) VALUES (:id, :uid, :mid, :amt, :notes)")
                .setParameter("id",    id)
                .setParameter("uid",   principal.userId())
                .setParameter("mid",   body.get("methodId") != null ? UUID.fromString(body.get("methodId").toString()) : null)
                .setParameter("amt",   new BigDecimal(body.get("amount").toString()))
                .setParameter("notes", body.get("notes"))
                .executeUpdate();
        return Map.of("id", id, "status", "PENDING", "message", "Payout request submitted");
    }

    @GetMapping("/payout-requests")
    @Transactional(readOnly = true)
    public Map<String, Object> myPayoutRequests(@AuthenticationPrincipal AuthPrincipal principal,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT id, amount, status, notes, created_at FROM payout_requests WHERE user_id = :uid ORDER BY created_at DESC LIMIT :lim OFFSET :off")
                .setParameter("uid", principal.userId())
                .setParameter("lim", size)
                .setParameter("off", page * size)
                .getResultList();

        List<Map<String, Object>> items = rows.stream().map(r -> Map.of(
                "id", r[0], "amount", r[1], "status", r[2], "notes", r[3] == null ? "" : r[3], "created_at", r[4]
        )).toList();

        return Map.of("content", items, "totalElements", items.size(), "page", page, "size", size);
    }

    // ── Admin payout management ───────────────────────────────────────────────

    @GetMapping("/admin/payout-requests")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Map<String, Object> allPayoutRequests(@RequestParam(required = false) String status,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        String where = status != null ? "WHERE status = '" + status + "'" : "";
        List<Object[]> rows = em.createNativeQuery(
                "SELECT id, user_id, amount, status, notes, created_at FROM payout_requests " + where + " ORDER BY created_at DESC LIMIT :lim OFFSET :off")
                .setParameter("lim", size)
                .setParameter("off", page * size)
                .getResultList();

        List<Map<String, Object>> items = rows.stream().map(r -> Map.of(
                "id", r[0], "user_id", r[1], "amount", r[2], "status", r[3], "notes", r[4] == null ? "" : r[4], "created_at", r[5]
        )).toList();

        return Map.of("content", items, "totalElements", items.size());
    }

    @PatchMapping("/admin/payout-requests/{id}/approve")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Map<String, Object> approvePayout(@PathVariable UUID id) {
        em.createNativeQuery("UPDATE payout_requests SET status = 'APPROVED', updated_at = NOW() WHERE id = :id")
                .setParameter("id", id).executeUpdate();
        return Map.of("message", "Payout approved");
    }

    @PatchMapping("/admin/payout-requests/{id}/reject")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Map<String, Object> rejectPayout(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        em.createNativeQuery("UPDATE payout_requests SET status = 'REJECTED', admin_note = :note, updated_at = NOW() WHERE id = :id")
                .setParameter("note", body.get("reason"))
                .setParameter("id",   id)
                .executeUpdate();
        return Map.of("message", "Payout rejected");
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private Map<String, Object> getOrCreateWallet(UUID userId) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT id, balance, currency, status FROM wallets WHERE user_id = :uid")
                .setParameter("uid", userId)
                .getResultList();

        if (rows.isEmpty()) {
            UUID walletId = UUID.randomUUID();
            em.createNativeQuery("INSERT INTO wallets (id, user_id, balance, currency, status) VALUES (:id, :uid, 0, 'EGP', 'ACTIVE')")
                    .setParameter("id",  walletId)
                    .setParameter("uid", userId)
                    .executeUpdate();
            return Map.of("id", walletId, "balance", BigDecimal.ZERO, "currency", "EGP", "status", "ACTIVE", "payout_methods", List.of());
        }

        Object[] r = rows.get(0);
        UUID walletId = (UUID) r[0];

        List<Object[]> methods = em.createNativeQuery(
                "SELECT id, type, account_name, account_number, bank_name, phone, is_default FROM payout_methods WHERE user_id = :uid")
                .setParameter("uid", userId)
                .getResultList();

        List<Map<String, Object>> payoutMethods = methods.stream().map(m -> {
            Map<String, Object> pm = new LinkedHashMap<>();
            pm.put("id",             m[0]);
            pm.put("type",           m[1]);
            pm.put("account_name",   m[2]);
            pm.put("account_number", m[3]);
            pm.put("bank_name",      m[4]);
            pm.put("phone",          m[5]);
            pm.put("is_default",     m[6]);
            return pm;
        }).toList();

        Map<String, Object> wallet = new LinkedHashMap<>();
        wallet.put("id",             walletId);
        wallet.put("balance",        r[1]);
        wallet.put("currency",       r[2]);
        wallet.put("status",         r[3]);
        wallet.put("payout_methods", payoutMethods);
        return wallet;
    }
}
