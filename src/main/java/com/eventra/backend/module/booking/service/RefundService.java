package com.eventra.backend.module.booking.service;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.booking.dto.request.RefundRequest;
import com.eventra.backend.module.booking.dto.response.RefundResponse;
import com.eventra.backend.module.booking.entity.Booking;
import com.eventra.backend.module.booking.entity.Payment;
import com.eventra.backend.module.booking.entity.Refund;
import com.eventra.backend.module.booking.enums.BookingStatus;
import com.eventra.backend.module.booking.gateway.StripeGateway;
import com.eventra.backend.module.booking.repository.BookingRepository;
import com.eventra.backend.module.booking.repository.PaymentRepository;
import com.eventra.backend.module.booking.repository.RefundRepository;
import com.eventra.backend.module.booking.repository.TicketRepository;
import com.eventra.backend.module.booking.valueobject.Money;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RefundService {

    private final RefundRepository refundRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final StripeGateway stripeGateway;

    public RefundService(RefundRepository refundRepository,
                         BookingRepository bookingRepository,
                         PaymentRepository paymentRepository,
                         TicketRepository ticketRepository,
                         StripeGateway stripeGateway) {
        this.refundRepository = refundRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
        this.stripeGateway = stripeGateway;
    }

    @Transactional
    public RefundResponse processRefund(UUID attendeeId, RefundRequest request) {
        Booking booking = bookingRepository.findByIdAndAttendeeId(
                        request.bookingId(), attendeeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "BOOKING_NOT_FOUND", "Booking not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "BOOKING_NOT_CONFIRMED", "Only confirmed bookings can be refunded");
        }

        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "PAYMENT_NOT_FOUND", "Payment record not found"));

        // Calculate partial refund amount based on quantity
        int totalQuantity = booking.getItems().stream()
                .mapToInt(item -> item.getQuantity())
                .sum();

        if (request.quantity() <= 0 || request.quantity() > totalQuantity) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "INVALID_REFUND_QUANTITY", "Cannot refund " + request.quantity() + " tickets for booking with " + totalQuantity + " tickets");
        }

        java.math.BigDecimal refundAmount = booking.getTotalAmount().getAmount()
                .multiply(java.math.BigDecimal.valueOf(request.quantity()))
                .divide(java.math.BigDecimal.valueOf(totalQuantity), 2, java.math.RoundingMode.HALF_UP);

        // Process refund via gateway
        stripeGateway.refund(payment.getTransactionId(), refundAmount);

        if (booking.getItems().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "NO_BOOKING_ITEMS", "Booking contains no items");
        }

        com.eventra.backend.module.booking.valueobject.BookingItem item = booking.getItems().get(0);
        int remainingQuantity = item.getQuantity() - request.quantity();

        // Release ticket capacity
        ticketRepository.findById(item.getTicketId()).ifPresent(ticket -> {
            ticket.release(request.quantity());
            ticketRepository.save(ticket);
        });

        if (remainingQuantity == 0) {
            booking.cancel();
            payment.setStatus(com.eventra.backend.module.booking.enums.PaymentStatus.REFUNDED);
        } else {
            item.setQuantity(remainingQuantity);
            booking.setTotalAmount(new Money(
                    booking.getTotalAmount().getAmount().subtract(refundAmount),
                    booking.getTotalAmount().getCurrency()
            ));
        }

        bookingRepository.save(booking);
        paymentRepository.save(payment);

        // Save refund record
        Refund refund = new Refund();
        refund.setPaymentId(payment.getId());
        refund.setBookingId(booking.getId());
        refund.setAmount(new Money(refundAmount, "EGP"));
        refund.setReason(request.reason());
        refund.process();
        refundRepository.save(refund);

        return RefundResponse.from(refund);
    }
}