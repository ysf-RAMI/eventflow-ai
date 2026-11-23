package com.example.bookingservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.util.UUID;

@FeignClient(
        name = "payment-service"
)
public interface PaymentServiceRestClient {

    @PostMapping("/internal/payments/simulate")
    PaymentResponse simulate(@RequestBody SimulatePaymentRequest request);

    @GetMapping("/internal/payments/by-booking/{bookingId}")
    PaymentResponse findByBookingId(@PathVariable UUID bookingId);

    enum PaymentStatus {
        PENDING,
        SUCCEEDED,
        FAILED
    }

    record SimulatePaymentRequest(UUID bookingId, Long amountCents) {
    }

    record PaymentResponse(
            UUID id,
            UUID bookingId,
            long amountCents,
            PaymentStatus status,
            Instant createdAt
    ) {
    }
}
