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
    private static final int MAX_TITLE_LENGTH = 200;

    @Id
    private UUID id;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
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


    public void update(String title, String description, LocalDateTime date) {
        if (status != EventStatus.DRAFT) {
            throw new IllegalStateException("Only draft events can be updated");
        }
        requireTitleAndDate(title, date);
        this.title = title;
        this.description = description;
        this.date = date;
    }

    public void requireDeletable() {
        if (status != EventStatus.DRAFT) {
            throw new IllegalStateException("Only draft events can be deleted");
        }
    }

    private static void requireTitleAndDate(String title, LocalDateTime date) {
        if (title == null || title.isBlank() || date == null) {
            throw new IllegalArgumentException("Title and date are required");
        }
        if (title.codePointCount(0, title.length())  > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Title must not exceed " + MAX_TITLE_LENGTH + " characters");
        }
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
