package com.example.bookingservice.dto;

import java.util.List;
import java.util.UUID;

public record CreateBookingRequest(
        UUID customerId,
        UUID eventId,
        List<BookingItemRequest> items
) {
}
