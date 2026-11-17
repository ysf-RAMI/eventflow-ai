package com.eventflow.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    private UUID id;

    @Column(name = "booking_id", nullable = false, unique = true)
    private UUID bookingId;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Payment() {
    }

    public static Payment create(UUID bookingId, long amountCents, Instant createdAt) {
        if (bookingId == null || amountCents < 0 || createdAt == null) {
            throw new IllegalArgumentException("Booking, non-negative amount and creation time are required");
        }
        Payment payment = new Payment();
        payment.id = UUID.randomUUID();
        payment.bookingId = bookingId;
        payment.amountCents = amountCents;
        payment.createdAt = createdAt;
        payment.status = PaymentStatus.PENDING;
        return payment;
    }

    public void succeed() {
        requirePending();
        status = PaymentStatus.SUCCEEDED;
    }

    public void fail() {
        requirePending();
        status = PaymentStatus.FAILED;
    }

    public UUID getId() { return id; }
    public UUID getBookingId() { return bookingId; }
    public long getAmountCents() { return amountCents; }
    public PaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    private void requirePending() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is already " + status);
        }
    }
}
