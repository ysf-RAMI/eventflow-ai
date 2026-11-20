package com.example.bookingservice.inventory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface InventoryClient {

    List<SeatStatus> getSeatStatuses(UUID eventId, List<UUID> seatIds);

    HoldResponse holdSeats(UUID eventId, List<UUID> seatIds, Instant expiresAt);

    void releaseHold(UUID holdId);

    void confirmHold(UUID holdId);

    enum SeatStatus {
        AVAILABLE,
        HELD,
        SOLD,
        UNKNOWN
    }

    record HoldResponse(UUID holdId, Instant expiresAt) {
    }
}
