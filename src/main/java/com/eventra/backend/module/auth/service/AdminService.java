package com.eventra.backend.module.auth.service;

import com.eventra.backend.module.auth.dto.response.*;
import com.eventra.backend.module.auth.entity.User;
import com.eventra.backend.module.auth.entity.UserRole;
import com.eventra.backend.module.auth.entity.UserStatus;
import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.auth.repository.OrganizerProfileRepository;
import com.eventra.backend.module.auth.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final AuditService auditService;
    private final TokenService tokenService;

    public AdminService(UserRepository userRepository, OrganizerProfileRepository organizerProfileRepository, AuditService auditService, TokenService tokenService) {
        this.userRepository = userRepository;
        this.organizerProfileRepository = organizerProfileRepository;
        this.auditService = auditService;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrganizerSummaryResponse> pendingOrganizers(int page, int size) {
        var users = userRepository.findByRoleAndStatus(UserRole.ORGANIZER, UserStatus.PENDING_ADMIN_APPROVAL, PageRequest.of(page, size));
        var data = users.stream()
                .map(user -> OrganizerSummaryResponse.from(user, organizerProfileRepository.findByUserId(user.getId()).orElseThrow()))
                .toList();
        return new PageResponse<>(data, users.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public OrganizerDetailResponse organizer(UUID id) {
        User user = load(id);
        var profile = organizerProfileRepository.findByUserId(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Organizer profile not found"));
        return OrganizerDetailResponse.from(user, profile);
    }

    @Transactional
    public void approveOrganizer(UUID adminId, UUID id, String ipAddress) {
        User user = load(id);
        requireStatus(user, UserStatus.PENDING_ADMIN_APPROVAL);
        UserStatus previous = user.getStatus();
        user.setStatus(UserStatus.ACTIVE);
        var profile = organizerProfileRepository.findByUserId(id).orElseThrow();
        profile.setApprovedBy(adminId);
        profile.setApprovedAt(Instant.now());
        auditService.log(adminId, id, "ORGANIZER_APPROVED", previous, UserStatus.ACTIVE, null, ipAddress);
    }

    @Transactional
    public void rejectOrganizer(UUID adminId, UUID id, String reason, String ipAddress) {
        User user = load(id);
        requireStatus(user, UserStatus.PENDING_ADMIN_APPROVAL);
        UserStatus previous = user.getStatus();
        user.setStatus(UserStatus.REJECTED);
        var profile = organizerProfileRepository.findByUserId(id).orElseThrow();
        profile.setRejectionReason(reason);
        auditService.log(adminId, id, "ORGANIZER_REJECTED", previous, UserStatus.REJECTED, reason, ipAddress);
    }

    @Transactional
    public void suspend(UUID adminId, UUID id, String reason, String ipAddress) {
        changeStatusWithRevocation(adminId, id, UserStatus.SUSPENDED, "USER_SUSPENDED", reason, ipAddress);
    }

    @Transactional
    public void ban(UUID adminId, UUID id, String reason, String ipAddress) {
        changeStatusWithRevocation(adminId, id, UserStatus.BANNED, "USER_BANNED", reason, ipAddress);
    }

    @Transactional
    public void disable(UUID adminId, UUID id, String reason, String ipAddress) {
        changeStatusWithRevocation(adminId, id, UserStatus.DISABLED, "USER_DISABLED", reason, ipAddress);
    }

    @Transactional
    public void reactivate(UUID adminId, UUID id, String ipAddress) {
        User user = load(id);
        requireStatus(user, UserStatus.SUSPENDED);
        UserStatus previous = user.getStatus();
        user.setStatus(UserStatus.ACTIVE);
        auditService.log(adminId, id, "USER_REACTIVATED", previous, UserStatus.ACTIVE, null, ipAddress);
    }

    private void changeStatusWithRevocation(UUID adminId, UUID id, UserStatus newStatus, String actionType, String reason, String ipAddress) {
        User user = load(id);
        UserStatus previous = user.getStatus();
        user.setStatus(newStatus);
        tokenService.bulkRevokeAndBlacklist(user);
        auditService.log(adminId, id, actionType, previous, newStatus, reason, ipAddress);
    }

    private void requireStatus(User user, UserStatus status) {
        if (user.getStatus() != status) {
            throw new ApiException(HttpStatus.CONFLICT, "WRONG_STATUS", "User is not in the required status");
        }
    }

    private User load(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));
    }
}
