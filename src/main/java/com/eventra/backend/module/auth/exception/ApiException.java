package com.eventra.backend.module.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String error;
    private final Map<String, Object> extra;

    public ApiException(HttpStatus status, String error, String message) {
        this(status, error, message, Map.of());
    }

    public ApiException(HttpStatus status, String error, String message, Map<String, Object> extra) {
        super(message);
        this.status = status;
        this.error = error;
        this.extra = extra;
    }
}
