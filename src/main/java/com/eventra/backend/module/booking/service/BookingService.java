package com.eventra.backend.module.booking.service;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.booking.dto.request.BookingRequest;
import com.eventra.backend.module.booking.dto.response.BookingResponse;
import com.eventra.backend.module.booking.entity.Booking;
import com.eventra.backend.module.booking.entity.Ticket;
import com.eventra.backend.module.booking.enums.BookingStatus;
import com.eventra.backend.module.booking.repository.BookingRepository;
import com.eventra.backend.module.booking.repository.TicketRepository;
import com.eventra.backend.module.booking.valueobject.BookingItem;
import com.eventra.backend.module.booking.valueobject.Money;
import com.eventra.backend.module.event.enums.EventStatus;
import com.eventra.backend.module.event.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    public BookingService(BookingRepository bookingRepository,
                          TicketRepository ticketRepository,
                          EventRepository eventRepository) {
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public BookingResponse createBooking(UUID attendeeId, BookingRequest request) {
        // Verify event exists and is published
        var event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND", "Event not found"));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "EVENT_NOT_PUBLISHED", "Event is not open for bookings");
        }

        if (event.getDateTime().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "EVENT_ALREADY_PASSED", "Cannot book tickets for past events");
        }

        // Prevent duplicate bookings
        if (bookingRepository.existsByAttendeeIdAndEventIdAndStatus(
                attendeeId, request.eventId(), BookingStatus.CONFIRMED)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "ALREADY_BOOKED", "You already have a confirmed booking for this event");
        }

        // Get and reserve ticket
        Ticket ticket = ticketRepository.findByIdForUpdate(request.ticketId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "TICKET_NOT_FOUND", "Ticket not found"));

        if (!ticket.reserve(request.quantity())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "INSUFFICIENT_TICKETS", "Not enough tickets available");
        }
        ticketRepository.save(ticket);

        // Calculate total
        BigDecimal total = ticket.getPrice().getAmount()
                .multiply(BigDecimal.valueOf(request.quantity()));

        // Build booking item
        BookingItem item = new BookingItem();
        item.setTicketId(ticket.getId());
        item.setQuantity(request.quantity());

        // Create booking with 15-minute hold
        Booking booking = new Booking();
        booking.setAttendeeId(attendeeId);
        booking.setEventId(request.eventId());
        booking.getItems().add(item);
        booking.setTotalAmount(new Money(total, "EGP"));
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setHoldExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));

        return BookingResponse.from(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(UUID attendeeId, UUID bookingId) {
        return bookingRepository.findByIdAndAttendeeId(bookingId, attendeeId)
                .map(BookingResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "BOOKING_NOT_FOUND", "Booking not found"));
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getMyBookings(UUID attendeeId, int page, int size) {
        return bookingRepository.findByAttendeeId(
                        attendeeId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(BookingResponse::from);
    }

    @Transactional
    public void cancelBooking(UUID attendeeId, UUID bookingId) {
        Booking booking = bookingRepository.findByIdAndAttendeeId(bookingId, attendeeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "BOOKING_NOT_FOUND", "Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "ALREADY_CANCELLED", "Booking is already cancelled");
        }

        var event = eventRepository.findById(booking.getEventId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND", "Event not found"));
        if (event.getDateTime().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "EVENT_ALREADY_PASSED", "Cannot cancel a booking for an event that has already happened");
        }

        booking.cancel();
        booking.getItems().forEach(item ->
                ticketRepository.findByIdForUpdate(item.getTicketId()).ifPresent(ticket -> {
                    ticket.release(item.getQuantity());
                    ticketRepository.save(ticket);
                })
        );
        bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getEventBookings(UUID organizerId, UUID eventId) {
        assertEventOwner(eventId, organizerId);
        return bookingRepository.findByEventId(eventId)
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse checkIn(UUID organizerId, UUID eventId, UUID attendeeId) {
        assertEventOwner(eventId, organizerId);
        List<Booking> bookings = bookingRepository.findByEventIdAndStatus(
                eventId, BookingStatus.CONFIRMED);

        Booking booking = bookings.stream()
                .filter(b -> b.getAttendeeId().equals(attendeeId))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "BOOKING_NOT_FOUND", "No confirmed booking found for this attendee"));

        if (booking.isCheckedIn()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "ALREADY_CHECKED_IN", "Attendee is already checked in");
        }

        booking.checkIn();
        return BookingResponse.from(bookingRepository.save(booking));
    }

    private void assertEventOwner(UUID eventId, UUID organizerId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND", "Event not found"));
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "NOT_EVENT_OWNER", "You do not own this event");
        }
    }

    // Runs every 5 minutes — releases expired holds and restores ticket capacity
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void releaseExpiredHolds() {
        List<Booking> expired = bookingRepository.findExpiredHolds(Instant.now());
        for (Booking booking : expired) {
            booking.cancel();
            booking.getItems().forEach(item ->
                    ticketRepository.findByIdForUpdate(item.getTicketId()).ifPresent(ticket -> {
                        ticket.release(item.getQuantity());
                        ticketRepository.save(ticket);
                    })
            );
            bookingRepository.save(booking);
        }
    }
}