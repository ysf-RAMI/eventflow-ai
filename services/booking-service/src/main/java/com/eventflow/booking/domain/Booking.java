package com.eventflow.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "booking")
public class Booking {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "total_cents")
    private Long totalCents;

    @Column(name = "hold_expires_at")
    private Instant holdExpiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Booking() {
    }

    public static Booking create(UUID customerId, UUID eventId, Instant createdAt) {
        if (customerId == null || eventId == null || createdAt == null) {
            throw new IllegalArgumentException("Customer, event and creation time are required");
        }
        Booking booking = new Booking();
        booking.id = UUID.randomUUID();
        booking.customerId = customerId;
        booking.eventId = eventId;
        booking.status = BookingStatus.PENDING;
        booking.createdAt = createdAt;
        return booking;
    }

    public void markHeld(long totalCents, Instant holdExpiresAt) {
        if (status != BookingStatus.PENDING || totalCents < 0 || holdExpiresAt == null) {
            throw new IllegalStateException("Booking cannot be held in its current state");
        }
        this.totalCents = totalCents;
        this.holdExpiresAt = holdExpiresAt;
        this.status = BookingStatus.HELD;
    }

    public void confirm() {
        requireStatus(BookingStatus.HELD);
        status = BookingStatus.CONFIRMED;
    }

    public void fail() {
        if (status != BookingStatus.PENDING && status != BookingStatus.HELD) {
            throw new IllegalStateException("Booking cannot fail in its current state");
        }
        status = BookingStatus.FAILED;
    }

    public void cancel() {
        requireStatus(BookingStatus.HELD);
        status = BookingStatus.CANCELLED;
    }

    public void expire() {
        requireStatus(BookingStatus.HELD);
        status = BookingStatus.EXPIRED;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public UUID getEventId() { return eventId; }
    public BookingStatus getStatus() { return status; }
    public Long getTotalCents() { return totalCents; }
    public Instant getHoldExpiresAt() { return holdExpiresAt; }
    public Instant getCreatedAt() { return createdAt; }

    private void requireStatus(BookingStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Expected status " + expected + " but was " + status);
        }
    }
}
