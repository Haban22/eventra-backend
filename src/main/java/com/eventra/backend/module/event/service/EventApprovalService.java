package com.eventra.backend.module.event.service;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.event.dto.request.EventApprovalRequest;
import com.eventra.backend.module.event.dto.response.EventApprovalResponse;
import com.eventra.backend.module.event.entity.Event;
import com.eventra.backend.module.event.entity.EventApproval;
import com.eventra.backend.module.event.enums.ApprovalStatus;
import com.eventra.backend.module.event.enums.EventStatus;
import com.eventra.backend.module.event.repository.EventApprovalRepository;
import com.eventra.backend.module.event.repository.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class EventApprovalService {

    private final EventRepository eventRepository;
    private final EventApprovalRepository approvalRepository;

    public EventApprovalService(EventRepository eventRepository,
                                EventApprovalRepository approvalRepository) {
        this.eventRepository = eventRepository;
        this.approvalRepository = approvalRepository;
    }

    @Transactional
    public EventApprovalResponse approveEvent(UUID adminId, UUID eventId,
                                              EventApprovalRequest request) {
        Event event = findEventOrThrow(eventId);

        if (event.getStatus() != EventStatus.PENDING_APPROVAL) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "EVENT_NOT_PENDING", "Event is not pending approval");
        }

        EventApproval approval = new EventApproval();
        approval.setEventId(eventId);
        approval.setAdminId(adminId);
        approval.setStatus(request.status());
        approval.setFeedback(request.feedback());
        approval.setReviewedAt(Instant.now());
        approvalRepository.save(approval);

        if (request.status() == ApprovalStatus.APPROVED) {
            event.approve();
        } else if (request.status() == ApprovalStatus.REJECTED) {
            event.setStatus(EventStatus.DRAFT);
        } else {
            event.setStatus(EventStatus.DRAFT);
        }
        eventRepository.save(event);

        return EventApprovalResponse.from(approval);
    }

    @Transactional(readOnly = true)
    public EventApprovalResponse getApprovalForEvent(UUID eventId) {
        return approvalRepository.findByEventId(eventId)
                .map(EventApprovalResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "APPROVAL_NOT_FOUND", "No approval record found for this event"));
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Event findEventOrThrow(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND", "Event not found"));
    }
}