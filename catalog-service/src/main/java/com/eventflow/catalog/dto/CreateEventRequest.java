package com.eventflow.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateEventRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        @NotNull LocalDateTime date
) {
}