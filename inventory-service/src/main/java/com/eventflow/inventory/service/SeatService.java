package com.eventflow.inventory.service;


import com.eventflow.inventory.domain.Seat;
import com.eventflow.inventory.dto.ProvisionSeatsResponse;
import com.eventflow.inventory.dto.SeatSpec;
import com.eventflow.inventory.repository.SeatRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SeatService {



    public record ProvisionResult(ProvisionSeatsResponse response, boolean created) {}

    private final SeatRepository seatRepository;
    private final TransactionTemplate transactionTemplate;

    public SeatService(SeatRepository seatRepository, TransactionTemplate transactionTemplate) {
        this.seatRepository = seatRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public ProvisionResult provision(UUID eventId, List<SeatSpec> specs) {
        validateNoDuplicateLabels(specs);

        if (seatRepository.existsByEventId(eventId)) {
            int existingCount = Math.toIntExact(seatRepository.countByEventId(eventId));
            return new ProvisionResult(new ProvisionSeatsResponse(eventId, existingCount), false);
        }

        try {
            int createdCount = transactionTemplate.execute(status -> {
                List<Seat> seats = specs.stream()
                        .map(spec -> Seat.create(eventId, spec.label(), spec.section(), spec.priceCents()))
                        .toList();
                seatRepository.saveAll(seats);
                return seats.size();
            });
            return new ProvisionResult(new ProvisionSeatsResponse(eventId, createdCount), true);
        } catch (DataIntegrityViolationException ex) {
            // Concurrent publish won the race; treat as idempotent 200 OK
            int existingCount = Math.toIntExact(seatRepository.countByEventId(eventId));
            return new ProvisionResult(new ProvisionSeatsResponse(eventId, existingCount), false);
        }
    }

    private void validateNoDuplicateLabels(List<SeatSpec> specs) {
        Set<String> seen = new HashSet<>(specs.size());
        for (SeatSpec spec : specs) {
            if (!seen.add(spec.label())) {
                throw new IllegalArgumentException("Duplicate seat label in request: " + spec.label());
            }
        }
    }
}
