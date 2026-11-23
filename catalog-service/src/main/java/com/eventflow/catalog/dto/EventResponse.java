package com.eventflow.catalog.dto;

import com.eventflow.catalog.domain.Event;
import com.eventflow.catalog.domain.EventStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventResponse(
        UUID id,
        UUID createdBy,
        String title,
        String description,
        LocalDateTime date,
        EventStatus status
) {

    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getCreatedBy(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getStatus()
        );
    }
}