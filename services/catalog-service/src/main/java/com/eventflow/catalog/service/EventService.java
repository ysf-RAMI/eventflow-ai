package com.eventflow.catalog.service;

import com.eventflow.catalog.domain.Event;
import com.eventflow.catalog.domain.EventStatus;
import com.eventflow.catalog.dto.CreateEventRequest;
import com.eventflow.catalog.dto.EventResponse;
import com.eventflow.catalog.dto.UpdateEventRequest;
import com.eventflow.catalog.exception.EventNotFoundException;
import com.eventflow.catalog.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EventService {

    private static final LocalDateTime MIN_DATE = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime MAX_DATE = LocalDateTime.of(2030, 12, 31, 23, 59);

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public EventResponse create(UUID createdBy, CreateEventRequest request) {
        Event event = Event.create(createdBy, request.title(), request.description(), request.date());
        return EventResponse.from(eventRepository.save(event));
    }

    @Transactional
    public EventResponse update(UUID id, UpdateEventRequest request) {
        Event event = findOrThrow(id);
        event.update(request.title(), request.description(), request.date());
        return EventResponse.from(event);
    }

    @Transactional
    public EventResponse publish(UUID id) {
        Event event = findOrThrow(id);
        event.publish();
        return EventResponse.from(event);
    }

    @Transactional
    public void delete(UUID id) {
        Event event = findOrThrow(id);
        event.requireDeletable();
        eventRepository.delete(event);
    }

    public EventResponse getById(UUID id) {
        Event event = findOrThrow(id);
        // Drafts are not visible to customers; admin-only access arrives with Keycloak (T-050).
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new EventNotFoundException(id);
        }
        return EventResponse.from(event);
    }

    public List<EventResponse> search(String query, LocalDateTime dateFrom, LocalDateTime dateTo) {
        String text = query == null ? "" : query.trim();
        LocalDateTime from = dateFrom != null ? dateFrom : MIN_DATE;
        LocalDateTime to = dateTo != null ? dateTo : MAX_DATE;
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("dateFrom must not be after dateTo");
        }
        return eventRepository
                .findByStatusAndTitleContainingIgnoreCaseAndDateBetweenOrderByDate(
                        EventStatus.PUBLISHED, text, from, to)
                .stream()
                .map(EventResponse::from)
                .toList();
    }

    private Event findOrThrow(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }
}