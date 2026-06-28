package com.eventra.backend.module.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Paginated wrapper for notification list responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPageResponse {

    private List<NotificationResponse> content;

    /** Zero-based current page index. */
    private int page;

    /** Requested page size. */
    private int size;

    /** Total number of notifications across all pages. */
    private long totalElements;

    /** Total number of pages. */
    private int totalPages;

    /** Whether this is the last page. */
    private boolean last;

    /** Whether this is the first page. */
    private boolean first;

    /**
     * Builds a paginated response from a Spring Data {@link Page} of notifications.
     *
     * @param page page of notification response DTOs
     * @return paginated notification response
     */
    public static NotificationPageResponse fromPage(Page<NotificationResponse> page) {
        return NotificationPageResponse.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
