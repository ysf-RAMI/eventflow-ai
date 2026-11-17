package com.eventflow.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event {

    @Id
    private UUID id;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    protected Event() {
    }

    public static Event create(UUID createdBy, String title, String description, LocalDateTime date) {
        if (createdBy == null || title == null || title.isBlank() || date == null) {
            throw new IllegalArgumentException("Creator, title and date are required");
        }
        Event event = new Event();
        event.id = UUID.randomUUID();
        event.createdBy = createdBy;
        event.title = title;
        event.description = description;
        event.date = date;
        event.status = EventStatus.DRAFT;
        return event;
    }

    public void publish() {
        if (status != EventStatus.DRAFT) {
            throw new IllegalStateException("Only draft events can be published");
        }
        status = EventStatus.PUBLISHED;
    }

    public UUID getId() { return id; }
    public UUID getCreatedBy() { return createdBy; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getDate() { return date; }
    public EventStatus getStatus() { return status; }
}
