package com.eventra.backend.module.config.repository;

import com.eventra.backend.module.config.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, Short> {
}
