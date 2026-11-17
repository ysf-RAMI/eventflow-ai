package com.eventflow.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

public record SimulatePaymentRequest(@NotNull UUID bookingId,
                                     @NotNull @PositiveOrZero Long amountCents) {}
