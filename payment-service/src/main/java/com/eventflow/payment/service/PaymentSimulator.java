package com.eventflow.payment.service;

import com.eventflow.payment.domain.PaymentStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentSimulator {
    private final PaymentStatus result;

    public PaymentSimulator(@Value("${payment.simulation.result:SUCCEEDED}") PaymentStatus result) {
        if (result != PaymentStatus.SUCCEEDED && result != PaymentStatus.FAILED) {
            throw new IllegalArgumentException("payment.simulation.result must be SUCCEEDED or FAILED");
        }
        this.result = result;
    }

    public PaymentStatus simulate() {
        return result;
    }
}
