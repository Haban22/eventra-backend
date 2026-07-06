package com.eventra.backend.module.booking.controller;

import com.eventra.backend.module.auth.dto.response.MessageResponse;
import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.booking.dto.request.BookingRequest;
import com.eventra.backend.module.booking.dto.response.BookingResponse;
import com.eventra.backend.module.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ATTENDEE')")
    public BookingResponse createBooking(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody BookingRequest request) {
        return bookingService.createBooking(principal.userId(), request);
    }

    @GetMapping("/{id}")
    public BookingResponse getBooking(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID id) {
        return bookingService.getBooking(principal.userId(), id);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('ATTENDEE')")
    public Page<BookingResponse> myBookings(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return bookingService.getMyBookings(principal.userId(), page, size);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ATTENDEE')")
    public MessageResponse cancelBooking(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID id) {
        bookingService.cancelBooking(principal.userId(), id);
        return new MessageResponse("Booking cancelled");
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public List<BookingResponse> eventBookings(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID eventId) {
        return bookingService.getEventBookings(principal.userId(), eventId);
    }

    @PostMapping("/event/{eventId}/checkin/{attendeeId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public BookingResponse checkIn(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID eventId,
            @PathVariable UUID attendeeId) {
        return bookingService.checkIn(principal.userId(), eventId, attendeeId);
    }
}