package com.example.bookingservice.inventory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("local")
public class StubInventoryClient implements InventoryClient {

    private final Map<UUID, Hold> holds = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> seatHolds = new ConcurrentHashMap<>();

    @Override
    public synchronized List<SeatStatus> getSeatStatuses(UUID eventId, List<UUID> seatIds) {
        if (eventId == null || seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("eventId and seatIds are required");
        }
        return seatIds.stream()
                .map(seatId -> seatHolds.containsKey(seatId) ? SeatStatus.HELD : SeatStatus.AVAILABLE)
                .toList();
    }

    @Override
    public synchronized HoldResponse holdSeats(UUID eventId, List<UUID> seatIds, Instant expiresAt) {
        if (eventId == null || seatIds == null || seatIds.isEmpty() || expiresAt == null) {
            throw new IllegalArgumentException("eventId, seatIds, and expiresAt are required");
        }
        if (!expiresAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("expiresAt must be in the future");
        }
        if (new HashSet<>(seatIds).size() != seatIds.size()) {
            throw new IllegalArgumentException("seatIds must not contain duplicates");
        }
        if (seatIds.stream().anyMatch(seatHolds::containsKey)) {
            throw new IllegalStateException("One or more seats are already held");
        }

        UUID holdId = UUID.randomUUID();
        holds.put(holdId, new Hold(eventId, List.copyOf(seatIds), expiresAt));
        seatIds.forEach(seatId -> seatHolds.put(seatId, holdId));
        return new HoldResponse(holdId, expiresAt);
    }

    @Override
    public synchronized void releaseHold(UUID holdId) {
        removeHold(holdId);
    }

    @Override
    public synchronized void confirmHold(UUID holdId) {
        removeHold(holdId);
    }

    private void removeHold(UUID holdId) {
        Hold hold = holds.remove(holdId);
        if (hold == null) {
            throw new IllegalArgumentException("Unknown inventory hold: " + holdId);
        }
        hold.seatIds().forEach(seatId -> seatHolds.remove(seatId, holdId));
    }

    private record Hold(UUID eventId, List<UUID> seatIds, Instant expiresAt) {
    }
}
