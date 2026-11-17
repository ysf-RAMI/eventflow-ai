package com.eventflow.booking.repository;

import com.eventflow.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
