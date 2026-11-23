package com.eventflow.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.UUID;

/**
 * A seat of one event. The status transitions here are the domain rules;
 * concurrent access is protected by the database (see T-022).
 */
@Entity
@Table(name = "event_seat")
public class Seat {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(nullable = false, length = 10)
    private String label;

    @Column(length = 50)
    private String section;

    @Column(name = "price_cents", nullable = false)
    private long priceCents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status;

    @Version
    private long version;

    protected Seat() {
        // required by JPA
    }

    public static Seat create(UUID eventId, String label, String section, long priceCents) {
        if (priceCents < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        Seat seat = new Seat();
        seat.id = UUID.randomUUID();
        seat.eventId = eventId;
        seat.label = label;
        seat.section = section;
        seat.priceCents = priceCents;
        seat.status = SeatStatus.AVAILABLE;
        return seat;
    }

    /** AVAILABLE to HELD. Anything else is rejected. */
    public void hold() {
        requireStatus(SeatStatus.AVAILABLE, "hold");
        this.status = SeatStatus.HELD;
    }

    /** HELD back to AVAILABLE, for example when a hold expires unpaid. */
    public void release() {
        requireStatus(SeatStatus.HELD, "release");
        this.status = SeatStatus.AVAILABLE;
    }

    /** HELD to SOLD, once payment succeeded and the hold is still valid. */
    public void sell() {
        requireStatus(SeatStatus.HELD, "sell");
        this.status = SeatStatus.SOLD;
    }

    private void requireStatus(SeatStatus expected, String action) {
        if (this.status != expected) {
            throw new IllegalStateException(
                    "Cannot " + action + " seat " + label + " in status " + status);
        }
    }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public String getLabel() { return label; }
    public String getSection() { return section; }
    public long getPriceCents() { return priceCents; }
    public SeatStatus getStatus() { return status; }
    public long getVersion() { return version; }
}
