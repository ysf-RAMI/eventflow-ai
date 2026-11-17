package com.eventflow.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "booking_item")
public class BookingItem {

    @Id
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "seat_id", nullable = false)
    private UUID seatId;

    @Column(name = "price_cents", nullable = false)
    private long priceCents;

    protected BookingItem() {
    }

    public static BookingItem create(UUID bookingId, UUID seatId, long priceCents) {
        if (bookingId == null || seatId == null || priceCents < 0) {
            throw new IllegalArgumentException("Booking, seat and non-negative price are required");
        }
        BookingItem item = new BookingItem();
        item.id = UUID.randomUUID();
        item.bookingId = bookingId;
        item.seatId = seatId;
        item.priceCents = priceCents;
        return item;
    }

    public UUID getId() { return id; }
    public UUID getBookingId() { return bookingId; }
    public UUID getSeatId() { return seatId; }
    public long getPriceCents() { return priceCents; }
}
