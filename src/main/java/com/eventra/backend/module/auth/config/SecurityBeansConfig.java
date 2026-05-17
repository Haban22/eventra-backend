package com.eventra.backend.module.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties({JwtConfig.class, AppProperties.class, CorsProperties.class})
public class SecurityBeansConfig {
    @Bean
    PasswordEncoder passwordEncoder(AppProperties properties) {
        return new BCryptPasswordEncoder(properties.bcryptStrength());
    }
}
