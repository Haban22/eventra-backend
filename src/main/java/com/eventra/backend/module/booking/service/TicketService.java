package com.eventra.backend.module.booking.service;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.booking.dto.request.TicketRequest;
import com.eventra.backend.module.booking.dto.response.TicketResponse;
import com.eventra.backend.module.booking.entity.Ticket;
import com.eventra.backend.module.booking.repository.TicketRepository;
import com.eventra.backend.module.booking.valueobject.Money;
import com.eventra.backend.module.event.repository.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    public TicketService(TicketRepository ticketRepository,
                         EventRepository eventRepository) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public TicketResponse createTicket(UUID eventId, TicketRequest request) {
        if (!eventRepository.existsById(eventId)) {
            throw new ApiException(HttpStatus.NOT_FOUND,
                    "EVENT_NOT_FOUND", "Event not found");
        }

        Ticket ticket = new Ticket();
        ticket.setEventId(eventId);
        ticket.setTicketType(request.ticketType());
        ticket.setTotalAvailable(request.totalAvailable());
        ticket.setSold(0);

        Money price = new Money(request.price(), "EGP");
        ticket.setPrice(price);

        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getEventTickets(UUID eventId) {
        return ticketRepository.findByEventId(eventId)
                .stream()
                .map(TicketResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .map(TicketResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "TICKET_NOT_FOUND", "Ticket not found"));
    }
}