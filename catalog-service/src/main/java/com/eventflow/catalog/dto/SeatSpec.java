package com.eventflow.catalog.dto;

public record SeatSpec(
        String label,
        String section,
        long priceCents
) {
}