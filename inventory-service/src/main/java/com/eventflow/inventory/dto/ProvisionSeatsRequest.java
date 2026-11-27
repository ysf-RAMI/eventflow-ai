package com.eventflow.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ProvisionSeatsRequest(
        @NotEmpty
        @Valid List<@Valid SeatSpec> seats
) {
}
