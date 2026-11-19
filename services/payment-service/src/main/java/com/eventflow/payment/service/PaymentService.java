package com.eventflow.payment.service;

import com.eventflow.payment.domain.Payment;
import com.eventflow.payment.domain.PaymentStatus;
import com.eventflow.payment.dto.PaymentResponse;
import com.eventflow.payment.dto.SimulatePaymentRequest;
import com.eventflow.payment.repository.PaymentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository repository;
    private final PaymentSimulator simulator;
    private final TransactionTemplate transaction;

    public PaymentService(PaymentRepository repository, PaymentSimulator simulator,
                          PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.simulator = simulator;
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public PaymentResponse simulate(SimulatePaymentRequest request) {
        try {
            return transaction.execute(status -> {
                var existing = repository.findByBookingId(request.bookingId());
                if (existing.isPresent()) {
                    return matchingResult(existing.get(), request.amountCents());
                }
                Payment payment = Payment.create(request.bookingId(), request.amountCents(), Instant.now());
                if (simulator.simulate() == PaymentStatus.SUCCEEDED) {
                    payment.succeed();
                } else {
                    payment.fail();
                }
                return PaymentResponse.from(repository.saveAndFlush(payment));
            });
        } catch (DataIntegrityViolationException conflict) {
            // A concurrent request may have inserted the same booking. Read only
            // after the failed transaction has rolled back, in a fresh transaction.
            return transaction.execute(status -> repository.findByBookingId(request.bookingId())
                    .map(payment -> matchingResult(payment, request.amountCents()))
                    .orElseThrow(() -> conflict));
        }
    }

    public PaymentResponse findByBookingId(UUID bookingId) {
        return repository.findByBookingId(bookingId).map(PaymentResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    private PaymentResponse matchingResult(Payment payment, long amountCents) {
        if (payment.getAmountCents() != amountCents) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A payment with a different amount already exists for this booking");
        }
        return PaymentResponse.from(payment);
    }
}
