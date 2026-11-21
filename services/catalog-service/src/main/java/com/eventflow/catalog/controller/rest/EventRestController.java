package com.eventflow.catalog.controller.rest;

import com.eventflow.catalog.dto.CreateEventRequest;
import com.eventflow.catalog.dto.EventResponse;
import com.eventflow.catalog.dto.UpdateEventRequest;
import com.eventflow.catalog.service.EventService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventRestController {

    private final EventService eventService;

    public EventRestController(EventService eventService) {
        this.eventService = eventService;
    }

    // ADMIN. TODO(T-050): replace X-User-Id with the Keycloak JWT subject.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(
            @RequestHeader("X-User-Id") UUID createdBy,
            @Valid @RequestBody CreateEventRequest request) {
        return eventService.create(createdBy, request);
    }

    // CUSTOMER + ADMIN. Published events only.
    @GetMapping
    public List<EventResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {
        return eventService.search(q, dateFrom, dateTo);
    }

    // CUSTOMER + ADMIN. Draft events are reported as not found.
    @GetMapping("/{id}")
    public EventResponse getById(@PathVariable UUID id) {
        return eventService.getById(id);
    }

    // ADMIN. Draft events only.
    @PutMapping("/{id}")
    public EventResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateEventRequest request) {
        return eventService.update(id, request);
    }

    // ADMIN. DRAFT -> PUBLISHED.
    @PostMapping("/{id}/publish")
    public EventResponse publish(@PathVariable UUID id) {
        return eventService.publish(id);
    }

    // ADMIN. Draft events only.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        eventService.delete(id);
    }
}