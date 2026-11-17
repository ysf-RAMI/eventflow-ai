package com.eventflow.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** A temporary reservation of one seat. Inventory owns the authoritative expiry. */
@Entity
@Table(name = "seat_hold")
public class SeatHold {

    @Id
    private UUID id;

    @Column(name = "seat_id", nullable = false)
    private UUID seatId;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HoldStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SeatHold() {
        // required by JPA
    }

    public static SeatHold create(UUID seatId, UUID bookingId, Instant expiresAt, Instant createdAt) {
        if (seatId == null || bookingId == null || expiresAt == null || createdAt == null) {
            throw new IllegalArgumentException("Seat, booking, expiry and creation time are required");
        }
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("Hold expiry must be after creation time");
        }
        SeatHold hold = new SeatHold();
        hold.id = UUID.randomUUID();
        hold.seatId = seatId;
        hold.bookingId = bookingId;
        hold.expiresAt = expiresAt;
        hold.createdAt = createdAt;
        hold.status = HoldStatus.ACTIVE;
        return hold;
    }

    public void confirm() {
        requireActive("confirm");
        this.status = HoldStatus.CONFIRMED;
    }

    public void release() {
        requireActive("release");
        this.status = HoldStatus.RELEASED;
    }

    public void expire(Instant now) {
        requireActive("expire");
        if (now == null || expiresAt.isAfter(now)) {
            throw new IllegalStateException("Cannot expire a hold before its expiry time");
        }
        this.status = HoldStatus.EXPIRED;
    }

    private void requireActive(String action) {
        if (status != HoldStatus.ACTIVE) {
            throw new IllegalStateException("Cannot " + action + " hold in status " + status);
        }
    }

    public UUID getId() { return id; }
    public UUID getSeatId() { return seatId; }
    public UUID getBookingId() { return bookingId; }
    public Instant getExpiresAt() { return expiresAt; }
    public HoldStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
