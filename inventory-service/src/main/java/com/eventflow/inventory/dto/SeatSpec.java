package com.eventflow.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SeatSpec(
        @NotBlank(message = "Seat label is required")
        @Size(max = 10, message = "Seat label must be at most 10 characters")
        String label,

        @Size(max = 50, message = "Section must be at most 50 characters")
        String section,

        @Min(value = 0, message = "Price cannot be negative")
        long priceCents) {
}
