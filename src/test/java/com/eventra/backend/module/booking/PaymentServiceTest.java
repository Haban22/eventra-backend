package com.eventra.backend.module.booking;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.booking.dto.request.PaymentRequest;
import com.eventra.backend.module.booking.dto.response.PaymentResponse;
import com.eventra.backend.module.booking.entity.Booking;
import com.eventra.backend.module.booking.entity.Payment;
import com.eventra.backend.module.booking.enums.BookingStatus;
import com.eventra.backend.module.booking.enums.PaymentMethod;
import com.eventra.backend.module.booking.enums.PaymentStatus;
import com.eventra.backend.module.booking.gateway.StripeGateway;
import com.eventra.backend.module.booking.repository.BookingRepository;
import com.eventra.backend.module.booking.repository.PaymentRepository;
import com.eventra.backend.module.booking.service.PaymentService;
import com.eventra.backend.module.booking.valueobject.Money;
import com.eventra.backend.module.event.repository.EventRepository;
import com.eventra.backend.module.wallet.service.WalletService;
import com.eventra.backend.module.notification.service.NotificationService;
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
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private StripeGateway stripeGateway;

    @Mock
    private WalletService walletService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPayment_Success() {
        UUID bookingId = UUID.randomUUID();
        PaymentRequest request = new PaymentRequest(
                bookingId, PaymentMethod.CREDIT_CARD, "John Doe", "1234567890123456", "12/29", "123");

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setTotalAmount(new Money(BigDecimal.valueOf(100), "EGP"));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(stripeGateway.processPayment(any(), any(), any())).thenReturn("txn_123");
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        PaymentResponse response = paymentService.processPayment(request);

        assertNotNull(response);
        assertEquals(bookingId, response.bookingId());
        assertEquals("COMPLETED", response.status());
        assertEquals("txn_123", response.transactionId());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals("txn_123", booking.getTransactionId());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void processPayment_BookingNotFound() {
        UUID bookingId = UUID.randomUUID();
        PaymentRequest request = new PaymentRequest(
                bookingId, PaymentMethod.CREDIT_CARD, "John Doe", "1234567890123456", "12/29", "123");

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                paymentService.processPayment(request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("BOOKING_NOT_FOUND", exception.getError());
    }

    @Test
    void processPayment_BookingNotPending() {
        UUID bookingId = UUID.randomUUID();
        PaymentRequest request = new PaymentRequest(
                bookingId, PaymentMethod.CREDIT_CARD, "John Doe", "1234567890123456", "12/29", "123");

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.CONFIRMED); // Already confirmed

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        ApiException exception = assertThrows(ApiException.class, () ->
                paymentService.processPayment(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("BOOKING_NOT_PENDING", exception.getError());
    }

    @Test
    void getPaymentByBooking_Success() {
        UUID bookingId = UUID.randomUUID();
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setBookingId(bookingId);
        payment.setAmount(new Money(BigDecimal.valueOf(100), "EGP"));
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentByBooking(bookingId);

        assertNotNull(response);
        assertEquals(bookingId, response.bookingId());
    }

    @Test
    void getPaymentByBooking_NotFound() {
        UUID bookingId = UUID.randomUUID();

        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                paymentService.getPaymentByBooking(bookingId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("PAYMENT_NOT_FOUND", exception.getError());
    }
}
