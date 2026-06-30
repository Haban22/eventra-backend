package com.eventra.backend.module.booking;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.booking.dto.request.BookingRequest;
import com.eventra.backend.module.booking.dto.response.BookingResponse;
import com.eventra.backend.module.booking.entity.Booking;
import com.eventra.backend.module.booking.entity.Ticket;
import com.eventra.backend.module.booking.enums.BookingStatus;
import com.eventra.backend.module.booking.enums.PaymentMethod;
import com.eventra.backend.module.booking.enums.TicketType;
import com.eventra.backend.module.booking.repository.BookingRepository;
import com.eventra.backend.module.booking.repository.TicketRepository;
import com.eventra.backend.module.booking.service.BookingService;
import com.eventra.backend.module.booking.valueobject.BookingItem;
import com.eventra.backend.module.booking.valueobject.Money;
import com.eventra.backend.module.event.entity.Event;
import com.eventra.backend.module.event.enums.EventStatus;
import com.eventra.backend.module.event.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBooking_Success() {
        UUID attendeeId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        BookingRequest request = new BookingRequest(eventId, ticketId, 2, PaymentMethod.CREDIT_CARD);

        Event event = new Event();
        event.setId(eventId);
        event.setStatus(EventStatus.PUBLISHED);
        event.setDateTime(Instant.now().plus(1, ChronoUnit.DAYS));

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setEventId(eventId);
        ticket.setTicketType(TicketType.GENERAL);
        ticket.setPrice(new Money(BigDecimal.valueOf(50), "EGP"));
        ticket.setTotalAvailable(10);
        ticket.setSold(0);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(bookingRepository.existsByAttendeeIdAndEventIdAndStatus(attendeeId, eventId, BookingStatus.CONFIRMED)).thenReturn(false);
        when(ticketRepository.findByIdForUpdate(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });

        BookingResponse response = bookingService.createBooking(attendeeId, request);

        assertNotNull(response);
        assertEquals(eventId, response.eventId());
        assertEquals(attendeeId, response.attendeeId());
        assertEquals(0, response.totalAmount().compareTo(BigDecimal.valueOf(100)));
        assertEquals("PENDING_PAYMENT", response.status());
        assertEquals(2, ticket.getSold());
    }

    @Test
    void createBooking_EventNotFound() {
        UUID attendeeId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        BookingRequest request = new BookingRequest(eventId, UUID.randomUUID(), 2, PaymentMethod.CREDIT_CARD);

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                bookingService.createBooking(attendeeId, request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("EVENT_NOT_FOUND", exception.getError());
    }

    @Test
    void createBooking_EventNotPublished() {
        UUID attendeeId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        BookingRequest request = new BookingRequest(eventId, UUID.randomUUID(), 2, PaymentMethod.CREDIT_CARD);

        Event event = new Event();
        event.setId(eventId);
        event.setStatus(EventStatus.DRAFT);
        event.setDateTime(Instant.now().plus(1, ChronoUnit.DAYS));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        ApiException exception = assertThrows(ApiException.class, () ->
                bookingService.createBooking(attendeeId, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("EVENT_NOT_PUBLISHED", exception.getError());
    }

    @Test
    void createBooking_EventInPast() {
        UUID attendeeId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        BookingRequest request = new BookingRequest(eventId, UUID.randomUUID(), 2, PaymentMethod.CREDIT_CARD);

        Event event = new Event();
        event.setId(eventId);
        event.setStatus(EventStatus.PUBLISHED);
        event.setDateTime(Instant.now().minus(1, ChronoUnit.DAYS));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        ApiException exception = assertThrows(ApiException.class, () ->
                bookingService.createBooking(attendeeId, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("EVENT_ALREADY_PASSED", exception.getError());
    }

    @Test
    void createBooking_AlreadyBooked() {
        UUID attendeeId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        BookingRequest request = new BookingRequest(eventId, UUID.randomUUID(), 2, PaymentMethod.CREDIT_CARD);

        Event event = new Event();
        event.setId(eventId);
        event.setStatus(EventStatus.PUBLISHED);
        event.setDateTime(Instant.now().plus(1, ChronoUnit.DAYS));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(bookingRepository.existsByAttendeeIdAndEventIdAndStatus(attendeeId, eventId, BookingStatus.CONFIRMED)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () ->
                bookingService.createBooking(attendeeId, request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("ALREADY_BOOKED", exception.getError());
    }

    @Test
    void createBooking_TicketNotFound() {
        UUID attendeeId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        BookingRequest request = new BookingRequest(eventId, ticketId, 2, PaymentMethod.CREDIT_CARD);

        Event event = new Event();
        event.setId(eventId);
        event.setStatus(EventStatus.PUBLISHED);
        event.setDateTime(Instant.now().plus(1, ChronoUnit.DAYS));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(bookingRepository.existsByAttendeeIdAndEventIdAndStatus(attendeeId, eventId, BookingStatus.CONFIRMED)).thenReturn(false);
        when(ticketRepository.findByIdForUpdate(ticketId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                bookingService.createBooking(attendeeId, request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("TICKET_NOT_FOUND", exception.getError());
    }

    @Test
    void createBooking_InsufficientTickets() {
        UUID attendeeId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        BookingRequest request = new BookingRequest(eventId, ticketId, 5, PaymentMethod.CREDIT_CARD);

        Event event = new Event();
        event.setId(eventId);
        event.setStatus(EventStatus.PUBLISHED);
        event.setDateTime(Instant.now().plus(1, ChronoUnit.DAYS));

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setEventId(eventId);
        ticket.setTicketType(TicketType.GENERAL);
        ticket.setPrice(new Money(BigDecimal.valueOf(50), "EGP"));
        ticket.setTotalAvailable(3);
        ticket.setSold(0);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(bookingRepository.existsByAttendeeIdAndEventIdAndStatus(attendeeId, eventId, BookingStatus.CONFIRMED)).thenReturn(false);
        when(ticketRepository.findByIdForUpdate(ticketId)).thenReturn(Optional.of(ticket));

        ApiException exception = assertThrows(ApiException.class, () ->
                bookingService.createBooking(attendeeId, request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("INSUFFICIENT_TICKETS", exception.getError());
    }

    @Test
    void getBooking_Success() {
        UUID attendeeId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setAttendeeId(attendeeId);
        booking.setTotalAmount(new Money(BigDecimal.valueOf(50), "EGP"));

        when(bookingRepository.findByIdAndAttendeeId(bookingId, attendeeId)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.getBooking(attendeeId, bookingId);

        assertNotNull(response);
        assertEquals(bookingId, response.id());
    }

    @Test
    void getBooking_NotFound() {
        UUID attendeeId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();

        when(bookingRepository.findByIdAndAttendeeId(bookingId, attendeeId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                bookingService.getBooking(attendeeId, bookingId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("BOOKING_NOT_FOUND", exception.getError());
    }

    @Test
    void getEventBookings_Success() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Event event = new Event();
        event.setId(eventId);
        event.setOrganizerId(organizerId);

        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setEventId(eventId);
        booking.setTotalAmount(new Money(BigDecimal.valueOf(100), "EGP"));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(bookingRepository.findByEventId(eventId)).thenReturn(List.of(booking));

        List<BookingResponse> responses = bookingService.getEventBookings(organizerId, eventId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void getEventBookings_NotOwner() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Event event = new Event();
        event.setId(eventId);
        event.setOrganizerId(UUID.randomUUID()); // Different organizer

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        ApiException exception = assertThrows(ApiException.class, () ->
                bookingService.getEventBookings(organizerId, eventId));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("NOT_EVENT_OWNER", exception.getError());
    }

    @Test
    void checkIn_Success() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID attendeeId = UUID.randomUUID();

        Event event = new Event();
        event.setId(eventId);
        event.setOrganizerId(organizerId);

        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setEventId(eventId);
        booking.setAttendeeId(attendeeId);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCheckedIn(false);
        booking.setTotalAmount(new Money(BigDecimal.valueOf(100), "EGP"));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(bookingRepository.findByEventIdAndStatus(eventId, BookingStatus.CONFIRMED)).thenReturn(List.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponse response = bookingService.checkIn(organizerId, eventId, attendeeId);

        assertNotNull(response);
        assertTrue(response.checkedIn());
        assertNotNull(response.checkedInAt());
    }

    @Test
    void checkIn_AlreadyCheckedIn() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID attendeeId = UUID.randomUUID();

        Event event = new Event();
        event.setId(eventId);
        event.setOrganizerId(organizerId);

        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setEventId(eventId);
        booking.setAttendeeId(attendeeId);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCheckedIn(true); // Already checked in
        booking.setTotalAmount(new Money(BigDecimal.valueOf(100), "EGP"));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(bookingRepository.findByEventIdAndStatus(eventId, BookingStatus.CONFIRMED)).thenReturn(List.of(booking));

        ApiException exception = assertThrows(ApiException.class, () ->
                bookingService.checkIn(organizerId, eventId, attendeeId));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("ALREADY_CHECKED_IN", exception.getError());
    }

    @Test
    void releaseExpiredHolds_Success() {
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);

        BookingItem item = new BookingItem();
        UUID ticketId = UUID.randomUUID();
        item.setTicketId(ticketId);
        item.setQuantity(3);
        booking.getItems().add(item);

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setSold(5);

        when(bookingRepository.findExpiredHolds(any(Instant.class))).thenReturn(List.of(booking));
        when(ticketRepository.findByIdForUpdate(ticketId)).thenReturn(Optional.of(ticket));

        bookingService.releaseExpiredHolds();

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(2, ticket.getSold());
        verify(bookingRepository, times(1)).save(booking);
        verify(ticketRepository, times(1)).save(ticket);
    }
}
