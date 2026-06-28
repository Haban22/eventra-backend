package com.eventra.backend.security;

import com.eventra.backend.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility methods for accessing the currently authenticated user from the security context.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Returns the ID of the currently authenticated user.
     *
     * @return authenticated user ID
     * @throws UnauthorizedException if no authenticated user is present
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }

        throw new UnauthorizedException("Invalid authentication principal");
    }
}
