package com.eventflow.payment.dto;

import com.eventflow.payment.domain.Payment;
import com.eventflow.payment.domain.PaymentStatus;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(UUID id, UUID bookingId, long amountCents,
                              PaymentStatus status, Instant createdAt) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getBookingId(),
                payment.getAmountCents(), payment.getStatus(), payment.getCreatedAt());
    }
}
