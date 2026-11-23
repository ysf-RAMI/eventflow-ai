package com.eventflow.catalog.dto;

import java.util.List;

public record ProvisionSeatsRequest(
        List<SeatSpec> seats
) {
}