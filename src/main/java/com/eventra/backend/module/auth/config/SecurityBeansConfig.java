package com.eventra.backend.module.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;

@Configuration
@EnableConfigurationProperties({JwtConfig.class, AppProperties.class, CorsProperties.class})
public class SecurityBeansConfig {
    @Bean
    PasswordEncoder passwordEncoder(AppProperties properties) {
        return new BCryptPasswordEncoder(properties.bcryptStrength());
    }

    @Bean
    GoogleIdTokenVerifier googleIdTokenVerifier(AppProperties properties) {
        GoogleIdTokenVerifier.Builder builder = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance());
        if (properties.googleClientId() != null && !properties.googleClientId().isBlank()) {
            builder.setAudience(Collections.singletonList(properties.googleClientId()));
        }
        return builder.build();
    }
}
