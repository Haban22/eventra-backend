package com.eventra.backend.module.auth.service;

import com.eventra.backend.module.auth.dto.request.PasswordChangeRequest;
import com.eventra.backend.module.auth.dto.request.UpdateMeRequest;
import com.eventra.backend.module.auth.dto.response.UserResponse;
import com.eventra.backend.module.auth.entity.OrganizerProfile;
import com.eventra.backend.module.auth.entity.User;
import com.eventra.backend.module.auth.entity.UserRole;
import com.eventra.backend.module.auth.entity.UserStatus;
import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.auth.repository.OrganizerProfileRepository;
import com.eventra.backend.module.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("authUserService")
public class UserService {
    private final UserRepository userRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UserService(UserRepository userRepository, 
                       OrganizerProfileRepository organizerProfileRepository,
                       PasswordEncoder passwordEncoder, 
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.organizerProfileRepository = organizerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public UserResponse me(UUID userId) {
        return UserResponse.from(load(userId));
    }

    @Transactional
    public UserResponse updateMe(UUID userId, UpdateMeRequest request) {
        User user = load(userId);
        if (request.fullName() != null) user.setFullName(request.fullName().trim());
        if (request.phone() != null) user.setPhone(request.phone().isBlank() ? null : request.phone());
        if (request.profilePictureUrl() != null) user.setProfilePictureUrl(request.profilePictureUrl().isBlank() ? null : request.profilePictureUrl());
        if (request.languagePreference() != null) user.setLanguagePreference(request.languagePreference());
        if (request.notificationPreferences() != null) user.setNotificationPreferences(request.notificationPreferences());
        if (request.city() != null) user.setCity(request.city().isBlank() ? null : request.city());
        if (request.interests() != null) user.setInterests(request.interests());
        if (request.onboardingCompleted() != null) user.setOnboardingCompleted(request.onboardingCompleted());
        return UserResponse.from(user);
    }

    @Transactional
    public void requestOrganizerUpgrade(UUID userId, String reason) {
        User user = load(userId);
        if (user.getRole() == UserRole.ORGANIZER) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ALREADY_ORGANIZER", "User is already an organizer");
        }

        organizerProfileRepository.findByUserId(userId).ifPresentOrElse(
                profile -> {
                    if (reason != null) profile.setOrganizationDescription(reason);
                },
                () -> {
                    OrganizerProfile profile = new OrganizerProfile();
                    profile.setUser(user);
                    profile.setOrganizationName(user.getFullName() + "'s Organization");
                    profile.setOrganizationDescription(reason != null ? reason : "");
                    organizerProfileRepository.save(profile);
                }
        );
    }

    @Transactional
    public void changePassword(UUID userId, String accessJti, long accessExpEpoch, PasswordChangeRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PASSWORDS_DO_NOT_MATCH", "Passwords do not match");
        }
        User user = load(userId);
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "WRONG_CURRENT_PASSWORD", "Wrong current password");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SAME_AS_CURRENT", "New password must be different");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        tokenService.revokeAllForUser(user.getId());
        tokenService.blacklist(accessJti, accessExpEpoch - java.time.Instant.now().getEpochSecond());
    }

    private User load(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));
    }
}
