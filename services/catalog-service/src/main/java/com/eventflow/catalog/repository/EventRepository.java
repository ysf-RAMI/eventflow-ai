package com.eventflow.catalog.repository;

import com.eventflow.catalog.domain.Event;
import com.eventflow.catalog.domain.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByStatusAndDateBetweenOrderByDate(
            EventStatus status,
            LocalDateTime from,
            LocalDateTime to
    );
    List<Event> findByStatusAndTitleContainingIgnoreCaseAndDateBetweenOrderByDate(
            EventStatus status,
            String title,
            LocalDateTime from,
            LocalDateTime to
    );
}
