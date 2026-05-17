package com.eventra.backend.module.auth.dto.response;

import java.util.List;

public record PageResponse<T>(List<T> data, long total, int page, int size) {
}
