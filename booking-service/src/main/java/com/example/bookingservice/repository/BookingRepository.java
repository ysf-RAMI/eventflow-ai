package com.example.bookingservice.repository;

import com.example.bookingservice.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository

public interface BookingRepository extends JpaRepository<Booking, UUID> {

        public List<Booking> findByCustomerId(UUID customerId);

        public List<Booking> findByEventId(UUID eventId);

        public Booking findByInventoryHoldId(UUID inventoryHoldId);

}

