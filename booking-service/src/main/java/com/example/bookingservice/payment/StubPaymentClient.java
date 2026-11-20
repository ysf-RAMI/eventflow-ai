package com.example.bookingservice.payment;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("local")
public class StubPaymentClient implements PaymentClient {

    @Override
    public PaymentResponse charge(UUID bookingId, Long amountCents) {
        if (bookingId == null || amountCents == null || amountCents < 0) {
            return new PaymentResponse(null, false);
        }
        return new PaymentResponse(UUID.randomUUID(), true);
    }
}
