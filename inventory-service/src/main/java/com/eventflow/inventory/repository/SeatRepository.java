package com.eventflow.inventory.repository;

import com.eventflow.inventory.domain.Seat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

    List<Seat> findByEventId(UUID eventId);
    boolean existsByEventId(UUID eventId);
    long countByEventId(UUID eventId);
}
