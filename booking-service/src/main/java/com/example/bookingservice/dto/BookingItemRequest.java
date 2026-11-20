package com.example.bookingservice.dto;

import java.util.UUID;

public record BookingItemRequest(
        UUID seatId,
        Long priceCents
) {
}
