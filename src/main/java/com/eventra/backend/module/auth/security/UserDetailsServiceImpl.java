package com.eventra.backend.module.auth.security;

import com.eventra.backend.module.user.entity.User;
import com.eventra.backend.module.user.repository.UserRepository;
import com.eventra.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security {@link UserDetailsService} implementation.
 *
 * <p>Loads a {@link UserPrincipal} by email for use during the authentication
 * (login) flow. After login, JWT tokens are used directly — this service
 * is not called on subsequent requests.</p>
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        return new UserPrincipal(user.getId(), user.getEmail());
    }
}
