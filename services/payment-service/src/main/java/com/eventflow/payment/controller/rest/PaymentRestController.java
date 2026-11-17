package com.eventflow.payment.controller.rest;

import com.eventflow.payment.dto.PaymentResponse;
import com.eventflow.payment.dto.SimulatePaymentRequest;
import com.eventflow.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/internal/payments")
public class PaymentRestController {
    private final PaymentService service;

    public PaymentRestController(PaymentService service) {
        this.service = service;
    }

    @PostMapping("/simulate")
    public PaymentResponse simulate(@Valid @RequestBody SimulatePaymentRequest request) {
        return service.simulate(request);
    }

    @GetMapping("/by-booking/{bookingId}")
    public PaymentResponse findByBookingId(@PathVariable UUID bookingId) {
        return service.findByBookingId(bookingId);
    }
}
