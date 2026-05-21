package com.eventra.backend.module.event.service;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.event.dto.response.EventSummaryResponse;
import com.eventra.backend.module.event.entity.Bookmark;
import com.eventra.backend.module.event.repository.BookmarkRepository;
import com.eventra.backend.module.event.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final EventRepository eventRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository,
                           EventRepository eventRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public void addBookmark(UUID userId, UUID eventId) {
        // Verify event exists
        if (!eventRepository.existsById(eventId)) {
            throw new ApiException(HttpStatus.NOT_FOUND,
                    "EVENT_NOT_FOUND", "Event not found");
        }
        // Silently ignore if already bookmarked
        if (bookmarkRepository.existsByUserIdAndEventId(userId, eventId)) {
            return;
        }
        Bookmark bookmark = new Bookmark();
        bookmark.setUserId(userId);
        bookmark.setEventId(eventId);
        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void removeBookmark(UUID userId, UUID eventId) {
        if (!bookmarkRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new ApiException(HttpStatus.NOT_FOUND,
                    "BOOKMARK_NOT_FOUND", "Bookmark not found");
        }
        bookmarkRepository.deleteByUserIdAndEventId(userId, eventId);
    }

    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> getBookmarks(UUID userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        return bookmarkRepository.findByUserId(userId, pageable)
                .map(bookmark -> eventRepository.findById(bookmark.getEventId())
                        .map(EventSummaryResponse::from)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                "EVENT_NOT_FOUND", "Event not found")));
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(UUID userId, UUID eventId) {
        return bookmarkRepository.existsByUserIdAndEventId(userId, eventId);
    }
}