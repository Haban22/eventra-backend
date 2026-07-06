package com.eventra.backend.module.auth.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class SecureTokenGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    private SecureTokenGenerator() {
    }

    public static String generate() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
