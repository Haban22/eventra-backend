package com.eventra.backend.module.booking;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.booking.dto.request.RefundRequest;
import com.eventra.backend.module.booking.dto.response.RefundResponse;
import com.eventra.backend.module.booking.entity.Booking;
import com.eventra.backend.module.booking.entity.Payment;
import com.eventra.backend.module.booking.entity.Refund;
import com.eventra.backend.module.booking.entity.Ticket;
import com.eventra.backend.module.booking.enums.BookingStatus;
import com.eventra.backend.module.booking.enums.PaymentStatus;
import com.eventra.backend.module.booking.enums.RefundStatus;
import com.eventra.backend.module.booking.gateway.StripeGateway;
import com.eventra.backend.module.booking.repository.BookingRepository;
import com.eventra.backend.module.booking.repository.PaymentRepository;
import com.eventra.backend.module.booking.repository.RefundRepository;
import com.eventra.backend.module.booking.repository.TicketRepository;
import com.eventra.backend.module.booking.service.RefundService;
import com.eventra.backend.module.booking.valueobject.BookingItem;
import com.eventra.backend.module.booking.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private StripeGateway stripeGateway;

    @InjectMocks
    private RefundService refundService;

    @Test
    void processRefund_FullRefund_Success() {
        UUID attendeeId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        RefundRequest request = new RefundRequest(bookingId, 2, "Duplicate booking");

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setAttendeeId(attendeeId);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalAmount(new Money(BigDecimal.valueOf(100), "EGP"));

        BookingItem item = new BookingItem();
        item.setTicketId(ticketId);
        item.setQuantity(2);
        booking.getItems().add(item);

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setBookingId(bookingId);
        payment.setTransactionId("txn_123");
        payment.setStatus(PaymentStatus.COMPLETED);

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setSold(5);

        when(bookingRepository.findByIdAndAttendeeId(bookingId, attendeeId)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.of(payment));
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> {
            Refund r = invocation.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        RefundResponse response = refundService.processRefund(attendeeId, request);

        assertNotNull(response);
        assertEquals(bookingId, response.bookingId());
        assertEquals("COMPLETED", response.status());
        assertEquals(0, response.amount().compareTo(BigDecimal.valueOf(100)));
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        assertEquals(3, ticket.getSold());

        verify(stripeGateway, times(1)).refund("txn_123", new BigDecimal("100.00"));
        verify(bookingRepository, times(1)).save(booking);
        verify(paymentRepository, times(1)).save(payment);
        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    void processRefund_PartialRefund_Success() {
        UUID attendeeId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        RefundRequest request = new RefundRequest(bookingId, 1, "Change of plans");

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setAttendeeId(attendeeId);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalAmount(new Money(BigDecimal.valueOf(100), "EGP"));

        BookingItem item = new BookingItem();
        item.setTicketId(ticketId);
        item.setQuantity(2);
        booking.getItems().add(item);

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setBookingId(bookingId);
        payment.setTransactionId("txn_123");
        payment.setStatus(PaymentStatus.COMPLETED);

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setSold(5);

        when(bookingRepository.findByIdAndAttendeeId(bookingId, attendeeId)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.of(payment));
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> {
            Refund r = invocation.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        RefundResponse response = refundService.processRefund(attendeeId, request);

        assertNotNull(response);
        assertEquals(bookingId, response.bookingId());
        assertEquals("COMPLETED", response.status());
        assertEquals(0, response.amount().compareTo(BigDecimal.valueOf(50))); // Refunded 50%
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus()); // Remains confirmed
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus()); // Payment is still completed (partially refunded)
        assertEquals(1, booking.getItems().get(0).getQuantity()); // Remains 1 ticket
        assertEquals(0, booking.getTotalAmount().getAmount().compareTo(BigDecimal.valueOf(50))); // Booking amount updated
        assertEquals(4, ticket.getSold()); // One ticket released

        verify(stripeGateway, times(1)).refund("txn_123", new BigDecimal("50.00"));
        verify(bookingRepository, times(1)).save(booking);
        verify(paymentRepository, times(1)).save(payment);
        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    void processRefund_InvalidQuantity_ThrowsException() {
        UUID attendeeId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        RefundRequest request = new RefundRequest(bookingId, 5, "Too many"); // 5 tickets but only 2 booked

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setAttendeeId(attendeeId);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalAmount(new Money(BigDecimal.valueOf(100), "EGP"));

        BookingItem item = new BookingItem();
        item.setQuantity(2);
        booking.getItems().add(item);

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setBookingId(bookingId);
        payment.setStatus(PaymentStatus.COMPLETED);

        when(bookingRepository.findByIdAndAttendeeId(bookingId, attendeeId)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.of(payment));

        ApiException exception = assertThrows(ApiException.class, () ->
                refundService.processRefund(attendeeId, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("INVALID_REFUND_QUANTITY", exception.getError());
    }

    @Test
    void processRefund_BookingNotFound() {
        UUID attendeeId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        RefundRequest request = new RefundRequest(bookingId, 1, "Reason");

        when(bookingRepository.findByIdAndAttendeeId(bookingId, attendeeId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                refundService.processRefund(attendeeId, request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("BOOKING_NOT_FOUND", exception.getError());
    }

    @Test
    void processRefund_BookingNotConfirmed() {
        UUID attendeeId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        RefundRequest request = new RefundRequest(bookingId, 1, "Reason");

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setAttendeeId(attendeeId);
        booking.setStatus(BookingStatus.PENDING_PAYMENT); // Not confirmed

        when(bookingRepository.findByIdAndAttendeeId(bookingId, attendeeId)).thenReturn(Optional.of(booking));

        ApiException exception = assertThrows(ApiException.class, () ->
                refundService.processRefund(attendeeId, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("BOOKING_NOT_CONFIRMED", exception.getError());
    }
}
