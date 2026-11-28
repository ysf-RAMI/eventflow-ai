package com.eventflow.inventory.controllerRest;

import com.eventflow.inventory.dto.ProvisionSeatsRequest;
import com.eventflow.inventory.dto.ProvisionSeatsResponse;
import com.eventflow.inventory.service.SeatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class InventoryRestController {
    private final SeatService seatService;

    public InventoryRestController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping("/internal/events/{eventId}/seats")
    public ResponseEntity<ProvisionSeatsResponse> provisionSeats(
            @PathVariable UUID eventId,
            @Valid @RequestBody ProvisionSeatsRequest request) {

        SeatService.ProvisionResult result = seatService.provision(eventId, request.seats());
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.response());
    }
}
