package com.eventflow.catalog.service;

import com.eventflow.catalog.client.InventoryClient;
import com.eventflow.catalog.domain.Event;
import com.eventflow.catalog.domain.EventStatus;
import com.eventflow.catalog.dto.CreateEventRequest;
import com.eventflow.catalog.dto.EventResponse;
import com.eventflow.catalog.dto.ProvisionSeatsRequest;
import com.eventflow.catalog.dto.UpdateEventRequest;
import com.eventflow.catalog.exception.EventNotFoundException;
import com.eventflow.catalog.exception.SeatProvisioningException;
import com.eventflow.catalog.repository.EventRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private static final LocalDateTime MIN_DATE = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime MAX_DATE = LocalDateTime.of(9999, 12, 31, 23, 59);

    private final EventRepository eventRepository;
    private final SeatGenerator seatGenerator;
    private final InventoryClient inventoryClient;

    public EventService(EventRepository eventRepository,
                        SeatGenerator seatGenerator,
                        InventoryClient inventoryClient) {
        this.eventRepository = eventRepository;
        this.seatGenerator = seatGenerator;
        this.inventoryClient = inventoryClient;
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
        // Throws IllegalStateException unless the event is DRAFT. The change is only saved
        // when the transaction commits, so a provisioning failure below rolls it back.
        event.publish();
        try {
            inventoryClient.provisionSeats(id, new ProvisionSeatsRequest(seatGenerator.generate(id)));
        } catch (FeignException ex) {
            log.warn("Seat provisioning failed for event {}: {}", id, ex.getMessage());
            throw new SeatProvisioningException(id, ex);
        }
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
