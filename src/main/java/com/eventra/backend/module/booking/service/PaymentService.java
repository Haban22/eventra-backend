package com.eventra.backend.module.booking.service;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.booking.dto.request.PaymentRequest;
import com.eventra.backend.module.booking.dto.response.PaymentResponse;
import com.eventra.backend.module.booking.entity.Booking;
import com.eventra.backend.module.booking.entity.Payment;
import com.eventra.backend.module.booking.enums.BookingStatus;
import com.eventra.backend.module.booking.enums.PaymentMethod;
import com.eventra.backend.module.booking.gateway.StripeGateway;
import com.eventra.backend.module.booking.repository.BookingRepository;
import com.eventra.backend.module.booking.repository.PaymentRepository;
import com.eventra.backend.module.event.repository.EventRepository;
import com.eventra.backend.module.wallet.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final StripeGateway stripeGateway;
    private final WalletService walletService;

    public PaymentService(PaymentRepository paymentRepository,
                          BookingRepository bookingRepository,
                          EventRepository eventRepository,
                          StripeGateway stripeGateway,
                          WalletService walletService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.stripeGateway = stripeGateway;
        this.walletService = walletService;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "BOOKING_NOT_FOUND", "Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "BOOKING_NOT_PENDING", "Booking is not awaiting payment");
        }

        String transactionId;
        if (request.paymentMethod() == PaymentMethod.WALLET) {
            // Debits the attendee's wallet directly instead of the card-mock gateway —
            // throws INSUFFICIENT_BALANCE (caught by the global exception handler) if
            // the wallet can't cover it, leaving the booking untouched.
            walletService.payWithWallet(booking.getAttendeeId(), booking.getTotalAmount().getAmount(), booking.getId());
            transactionId = "WALLET-" + booking.getId();
        } else {
            transactionId = stripeGateway.processPayment(
                    booking.getTotalAmount().getAmount(),
                    request.paymentMethod().name(),
                    request.cardNumber()
            );
        }

        // Save payment record
        Payment payment = new Payment();
        payment.setBookingId(booking.getId());
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setTransactionId(transactionId);
        payment.markSuccess();
        paymentRepository.save(payment);

        // Confirm booking
        booking.confirm();
        booking.setTransactionId(transactionId);
        bookingRepository.save(booking);

        // Credit the organizer's wallet with the booking total minus the platform fee
        eventRepository.findById(booking.getEventId()).ifPresent(event ->
                walletService.recordOrganizerEarning(event.getOrganizerId(), booking.getTotalAmount().getAmount(), booking.getId()));

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBooking(UUID bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "PAYMENT_NOT_FOUND", "Payment not found for this booking"));
    }
}