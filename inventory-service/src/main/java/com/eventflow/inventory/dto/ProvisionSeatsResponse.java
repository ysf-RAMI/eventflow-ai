package com.eventflow.inventory.dto;

import java.util.UUID;

public record ProvisionSeatsResponse(
        UUID eventId,
        int seatCount
)  {
}
