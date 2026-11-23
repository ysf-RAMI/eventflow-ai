package com.eventflow.inventory.repository;

import com.eventflow.inventory.domain.HoldStatus;
import com.eventflow.inventory.domain.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SeatHoldRepository extends JpaRepository<SeatHold, UUID> {

    List<SeatHold> findByBookingIdAndStatus(UUID bookingId, HoldStatus status);

    List<SeatHold> findByStatusAndExpiresAtLessThanEqual(HoldStatus status, Instant now);
}
