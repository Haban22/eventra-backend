package com.eventra.backend.module.calendar.service;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.calendar.dto.CreatePersonalEventRequest;
import com.eventra.backend.module.calendar.dto.PersonalEventResponse;
import com.eventra.backend.module.calendar.entity.PersonalEvent;
import com.eventra.backend.module.calendar.repository.PersonalEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PersonalEventService {
    private final PersonalEventRepository repository;

    public PersonalEventService(PersonalEventRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PersonalEventResponse> getMyEvents(UUID userId) {
        return repository.findByUserIdOrderByDateAsc(userId).stream().map(PersonalEventResponse::from).toList();
    }

    @Transactional
    public PersonalEventResponse create(UUID userId, CreatePersonalEventRequest request) {
        PersonalEvent event = new PersonalEvent();
        event.setUserId(userId);
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setDate(request.date());
        event.setEndDate(request.endDate());
        event.setLocation(request.location());
        event.setType(request.type());
        event.setCategory(request.category());
        return PersonalEventResponse.from(repository.save(event));
    }

    @Transactional
    public void delete(UUID userId, UUID eventId) {
        PersonalEvent event = repository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Personal event not found"));
        if (!event.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not own this event");
        }
        repository.delete(event);
    }
}
