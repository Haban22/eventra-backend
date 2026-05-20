package com.eventra.backend.module.auth.entity;

public enum UserStatus {
    PENDING_EMAIL_VERIFICATION,
    PENDING_ADMIN_APPROVAL,
    ACTIVE,
    REJECTED,
    SUSPENDED,
    BANNED,
    DISABLED
}
