package com.eventra.backend.module.event.controller;

import com.eventra.backend.module.auth.dto.response.MessageResponse;
import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.event.dto.response.EventSummaryResponse;
import com.eventra.backend.module.event.service.BookmarkService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping("/events/{id}/bookmark")
    public MessageResponse addBookmark(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID id) {
        bookmarkService.addBookmark(principal.userId(), id);
        return new MessageResponse("Event bookmarked");
    }

    @DeleteMapping("/events/{id}/bookmark")
    public MessageResponse removeBookmark(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID id) {
        bookmarkService.removeBookmark(principal.userId(), id);
        return new MessageResponse("Bookmark removed");
    }

    @GetMapping("/attendee/bookmarks")
    public Page<EventSummaryResponse> getBookmarks(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return bookmarkService.getBookmarks(principal.userId(), page, size);
    }

    @GetMapping("/events/{id}/bookmark")
    public boolean isBookmarked(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID id) {
        return bookmarkService.isBookmarked(principal.userId(), id);
    }
}