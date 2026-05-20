package com.eventra.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.eventra.backend.module.auth",
        "com.eventra.backend.common"
})
@EntityScan(basePackages = {
        "com.eventra.backend.module.auth.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.eventra.backend.module.auth.repository"
})
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
