package com.eventra.backend.module.auth;

import com.eventra.backend.module.auth.dto.response.*;
import com.eventra.backend.module.auth.entity.*;
import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.auth.repository.OrganizerProfileRepository;
import com.eventra.backend.module.auth.repository.UserRepository;
import com.eventra.backend.module.auth.service.AdminService;
import com.eventra.backend.module.auth.service.AuditService;
import com.eventra.backend.module.auth.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OrganizerProfileRepository organizerProfileRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private TokenService tokenService;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userRepository, organizerProfileRepository, auditService, tokenService);
    }

    @Test
    void pendingOrganizers_Success() {
        // Given
        int page = 0;
        int size = 10;
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Pending Organizer");
        user.setEmail("pending@example.com");

        OrganizerProfile profile = new OrganizerProfile();
        profile.setUser(user);
        profile.setOrganizationName("Pending Org");
        profile.setOrganizationDescription("Reason");

        Page<OrganizerProfile> pageResult = new PageImpl<>(List.of(profile));
        when(organizerProfileRepository.findByApprovedAtIsNullAndRejectionReasonIsNull(PageRequest.of(page, size)))
                .thenReturn(pageResult);

        // When
        PageResponse<OrganizerSummaryResponse> response = adminService.pendingOrganizers(page, size);

        // Then
        assertNotNull(response);
        assertEquals(1, response.data().size());
        assertEquals("Pending Org", response.data().get(0).organizationName());
        assertEquals(1, response.total());
    }

    @Test
    void organizer_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setFullName("Organizer Name");

        OrganizerProfile profile = new OrganizerProfile();
        profile.setUser(user);
        profile.setOrganizationName("Org Name");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(organizerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        // When
        OrganizerDetailResponse response = adminService.organizer(userId);

        // Then
        assertNotNull(response);
        assertEquals("Organizer Name", response.fullName());
        assertEquals("Org Name", response.organizationName());
    }

    @Test
    void organizer_NotFound_ThrowsException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> adminService.organizer(userId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("NOT_FOUND", exception.getError());
    }

    @Test
    void approveOrganizer_Success_PendingAdminApproval() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";

        User user = new User();
        user.setId(userId);
        user.setRole(UserRole.ORGANIZER);
        user.setStatus(UserStatus.PENDING_ADMIN_APPROVAL);

        OrganizerProfile profile = new OrganizerProfile();
        profile.setUser(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(organizerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        // When
        adminService.approveOrganizer(adminId, userId, ip);

        // Then
        assertEquals(UserRole.ORGANIZER, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(profile.getApprovedAt());
        assertEquals(adminId, profile.getApprovedBy());
        verify(auditService).log(eq(adminId), eq(userId), eq("ORGANIZER_APPROVED"), 
                eq(UserStatus.PENDING_ADMIN_APPROVAL), eq(UserStatus.ACTIVE), isNull(), eq(ip));
    }

    @Test
    void approveOrganizer_Success_Active() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";

        User user = new User();
        user.setId(userId);
        user.setRole(UserRole.ATTENDEE);
        user.setStatus(UserStatus.ACTIVE);

        OrganizerProfile profile = new OrganizerProfile();
        profile.setUser(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(organizerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        // When
        adminService.approveOrganizer(adminId, userId, ip);

        // Then
        assertEquals(UserRole.ORGANIZER, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(profile.getApprovedAt());
        assertEquals(adminId, profile.getApprovedBy());
        verify(auditService).log(eq(adminId), eq(userId), eq("ORGANIZER_APPROVED"), 
                eq(UserStatus.ACTIVE), eq(UserStatus.ACTIVE), isNull(), eq(ip));
    }

    @Test
    void approveOrganizer_WrongStatus_ThrowsConflict() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.BANNED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> adminService.approveOrganizer(adminId, userId, ip));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("WRONG_STATUS", exception.getError());
    }

    @Test
    void rejectOrganizer_Success_Attendee() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";
        String reason = "incomplete docs";

        User user = new User();
        user.setId(userId);
        user.setRole(UserRole.ATTENDEE);
        user.setStatus(UserStatus.ACTIVE);

        OrganizerProfile profile = new OrganizerProfile();
        profile.setUser(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(organizerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        // When
        adminService.rejectOrganizer(adminId, userId, reason, ip);

        // Then
        assertEquals(UserRole.ATTENDEE, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals(reason, profile.getRejectionReason());
        verify(auditService).log(eq(adminId), eq(userId), eq("ORGANIZER_REJECTED"), 
                eq(UserStatus.ACTIVE), eq(UserStatus.ACTIVE), eq(reason), eq(ip));
    }

    @Test
    void rejectOrganizer_Success_Organizer() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";
        String reason = "incomplete docs";

        User user = new User();
        user.setId(userId);
        user.setRole(UserRole.ORGANIZER);
        user.setStatus(UserStatus.PENDING_ADMIN_APPROVAL);

        OrganizerProfile profile = new OrganizerProfile();
        profile.setUser(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(organizerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        // When
        adminService.rejectOrganizer(adminId, userId, reason, ip);

        // Then
        assertEquals(UserRole.ORGANIZER, user.getRole());
        assertEquals(UserStatus.REJECTED, user.getStatus());
        assertEquals(reason, profile.getRejectionReason());
        verify(auditService).log(eq(adminId), eq(userId), eq("ORGANIZER_REJECTED"), 
                eq(UserStatus.PENDING_ADMIN_APPROVAL), eq(UserStatus.REJECTED), eq(reason), eq(ip));
    }

    @Test
    void suspend_Success() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";
        String reason = "spamming";

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        adminService.suspend(adminId, userId, reason, ip);

        // Then
        assertEquals(UserStatus.SUSPENDED, user.getStatus());
        verify(tokenService).bulkRevokeAndBlacklist(user);
        verify(auditService).log(eq(adminId), eq(userId), eq("USER_SUSPENDED"), 
                eq(UserStatus.ACTIVE), eq(UserStatus.SUSPENDED), eq(reason), eq(ip));
    }

    @Test
    void ban_Success() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";
        String reason = "tos violation";

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        adminService.ban(adminId, userId, reason, ip);

        // Then
        assertEquals(UserStatus.BANNED, user.getStatus());
        verify(tokenService).bulkRevokeAndBlacklist(user);
        verify(auditService).log(eq(adminId), eq(userId), eq("USER_BANNED"), 
                eq(UserStatus.ACTIVE), eq(UserStatus.BANNED), eq(reason), eq(ip));
    }

    @Test
    void suspendWithDetails_Success() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";
        String reason = "rules violation";
        Instant until = Instant.now().plusSeconds(3600);

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        adminService.suspendWithDetails(adminId, userId, reason, until, ip);

        // Then
        assertEquals(UserStatus.SUSPENDED, user.getStatus());
        assertEquals(reason, user.getSuspensionReason());
        assertEquals(until, user.getSuspendedUntil());
        verify(tokenService).bulkRevokeAndBlacklist(user);
        verify(auditService).log(eq(adminId), eq(userId), eq("USER_SUSPENDED_DETAILED"), 
                eq(UserStatus.ACTIVE), eq(UserStatus.SUSPENDED), eq(reason), eq(ip));
    }

    @Test
    void verifyOrganizer_Success() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";

        User user = new User();
        user.setId(userId);
        user.setRole(UserRole.ORGANIZER);
        user.setStatus(UserStatus.ACTIVE);

        OrganizerProfile profile = new OrganizerProfile();
        profile.setUser(user);
        profile.setVerified(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(organizerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        // When
        adminService.verifyOrganizer(adminId, userId, ip);

        // Then
        assertTrue(profile.isVerified());
        verify(auditService).log(eq(adminId), eq(userId), eq("ORGANIZER_VERIFIED"), 
                eq(UserStatus.ACTIVE), eq(UserStatus.ACTIVE), isNull(), eq(ip));
    }

    @Test
    void verifyOrganizer_NotAnOrganizer_ThrowsBadRequest() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";

        User user = new User();
        user.setId(userId);
        user.setRole(UserRole.ATTENDEE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> adminService.verifyOrganizer(adminId, userId, ip));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("NOT_AN_ORGANIZER", exception.getError());
    }

    @Test
    void forcePasswordReset_Success() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);
        user.setMustResetPassword(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        adminService.forcePasswordReset(adminId, userId, ip);

        // Then
        assertTrue(user.isMustResetPassword());
        verify(tokenService).bulkRevokeAndBlacklist(user);
        verify(auditService).log(eq(adminId), eq(userId), eq("FORCE_PASSWORD_RESET"), 
                eq(UserStatus.ACTIVE), eq(UserStatus.ACTIVE), isNull(), eq(ip));
    }

    @Test
    void disable_Success() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";
        String reason = "requested by user";

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        adminService.disable(adminId, userId, reason, ip);

        // Then
        assertEquals(UserStatus.DISABLED, user.getStatus());
        verify(tokenService).bulkRevokeAndBlacklist(user);
        verify(auditService).log(eq(adminId), eq(userId), eq("USER_DISABLED"), 
                eq(UserStatus.ACTIVE), eq(UserStatus.DISABLED), eq(reason), eq(ip));
    }

    @Test
    void reactivate_Success_ForSuspendedUser() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.SUSPENDED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        adminService.reactivate(adminId, userId, ip);

        // Then
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(auditService).log(eq(adminId), eq(userId), eq("USER_REACTIVATED"), 
                eq(UserStatus.SUSPENDED), eq(UserStatus.ACTIVE), isNull(), eq(ip));
    }

    @Test
    void reactivate_Success_ForBannedUser() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.BANNED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        adminService.reactivate(adminId, userId, ip);

        // Then
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(auditService).log(eq(adminId), eq(userId), eq("USER_REACTIVATED"), 
                eq(UserStatus.BANNED), eq(UserStatus.ACTIVE), isNull(), eq(ip));
    }

    @Test
    void reactivate_Success_ForDisabledUser() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.DISABLED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        adminService.reactivate(adminId, userId, ip);

        // Then
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(auditService).log(eq(adminId), eq(userId), eq("USER_REACTIVATED"), 
                eq(UserStatus.DISABLED), eq(UserStatus.ACTIVE), isNull(), eq(ip));
    }

    @Test
    void reactivate_WrongStatus_ThrowsConflict() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String ip = "127.0.0.1";

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> adminService.reactivate(adminId, userId, ip));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("WRONG_STATUS", exception.getError());
    }
}
