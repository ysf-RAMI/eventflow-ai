package com.example.bookingservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "inventory-service",
        url = "${inventory-service.url:http://localhost:8082}"
)
public interface InventoryServiceRestClient {

    @PostMapping("/internal/holds/status")
    List<SeatStatus> getSeatStatuses(@RequestBody StatusRequest request);

    @PostMapping("/internal/holds")
    HoldResponse holdSeats(@RequestBody HoldRequest request);

    @PostMapping("/internal/holds/{bookingId}/confirm")
    void confirmHold(@PathVariable UUID bookingId);

    @DeleteMapping("/internal/holds/{bookingId}")
    void releaseHold(@PathVariable UUID bookingId);

    enum SeatStatus {
        AVAILABLE,
        HELD,
        SOLD
    }

    record StatusRequest(UUID eventId, List<UUID> seatIds) {
    }

    record HoldRequest(UUID bookingId, UUID eventId, List<UUID> seatIds, Instant expiresAt) {
    }

    record HoldResponse(UUID holdId, Instant expiresAt) {
    }
}
