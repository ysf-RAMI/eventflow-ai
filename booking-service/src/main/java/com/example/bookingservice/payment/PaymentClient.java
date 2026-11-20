package com.example.bookingservice.payment;

import java.util.UUID;

public interface PaymentClient {

    PaymentResponse charge(UUID bookingId, Long amountCents);

    record PaymentResponse(UUID paymentId, boolean successful) {
    }
}
