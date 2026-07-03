package com.eventra.backend.module.event.service;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.event.dto.request.ScheduleItemRequest;
import com.eventra.backend.module.event.dto.response.ScheduleItemResponse;
import com.eventra.backend.module.event.entity.Event;
import com.eventra.backend.module.event.entity.ScheduleItem;
import com.eventra.backend.module.event.repository.EventRepository;
import com.eventra.backend.module.event.repository.ScheduleItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final ScheduleItemRepository scheduleItemRepository;
    private final EventRepository eventRepository;

    public ScheduleService(ScheduleItemRepository scheduleItemRepository, EventRepository eventRepository) {
        this.scheduleItemRepository = scheduleItemRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<ScheduleItemResponse> getScheduleByEventId(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found");
        }
        return scheduleItemRepository.findByEventIdOrderByOrderIndexAsc(eventId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleItemResponse addScheduleItem(UUID eventId, UUID organizerId, ScheduleItemRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found"));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_EVENT_OWNER", "You are not the organizer of this event");
        }

        if (request.startTime().isAfter(request.endTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SCHEDULE_TIMES", "Start time must be before or equal to end time");
        }

        ScheduleItem item = new ScheduleItem();
        item.setEventId(eventId);
        item.setTitle(request.title());
        item.setDescription(request.description());
        item.setSpeakerName(request.speakerName());
        item.setSpeakerAvatarUrl(request.speakerAvatarUrl());
        item.setType(request.type());
        item.setStartTime(request.startTime());
        item.setEndTime(request.endTime());
        item.setLocation(request.location());
        item.setOrderIndex(request.orderIndex());

        ScheduleItem saved = scheduleItemRepository.save(item);
        return mapToResponse(saved);
    }

    @Transactional
    public ScheduleItemResponse updateScheduleItem(UUID itemId, UUID organizerId, ScheduleItemRequest request) {
        ScheduleItem item = scheduleItemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SCHEDULE_ITEM_NOT_FOUND", "Schedule item not found"));

        Event event = eventRepository.findById(item.getEventId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found"));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_EVENT_OWNER", "You are not the organizer of this event");
        }

        if (request.startTime().isAfter(request.endTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SCHEDULE_TIMES", "Start time must be before or equal to end time");
        }

        item.setTitle(request.title());
        item.setDescription(request.description());
        item.setSpeakerName(request.speakerName());
        item.setSpeakerAvatarUrl(request.speakerAvatarUrl());
        item.setType(request.type());
        item.setStartTime(request.startTime());
        item.setEndTime(request.endTime());
        item.setLocation(request.location());
        item.setOrderIndex(request.orderIndex());

        ScheduleItem updated = scheduleItemRepository.save(item);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteScheduleItem(UUID itemId, UUID organizerId) {
        ScheduleItem item = scheduleItemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SCHEDULE_ITEM_NOT_FOUND", "Schedule item not found"));

        Event event = eventRepository.findById(item.getEventId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found"));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_EVENT_OWNER", "You are not the organizer of this event");
        }

        scheduleItemRepository.delete(item);
    }

    private ScheduleItemResponse mapToResponse(ScheduleItem item) {
        return new ScheduleItemResponse(
                item.getId(),
                item.getEventId(),
                item.getTitle(),
                item.getDescription(),
                item.getSpeakerName(),
                item.getSpeakerAvatarUrl(),
                item.getType(),
                item.getStartTime(),
                item.getEndTime(),
                item.getLocation(),
                item.getOrderIndex()
        );
    }
}
