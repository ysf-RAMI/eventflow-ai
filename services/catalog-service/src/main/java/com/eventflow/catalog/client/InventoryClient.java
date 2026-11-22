package com.eventflow.catalog.client;

import com.eventflow.catalog.dto.ProvisionSeatsRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "inventory", url = "${inventory.base-url}")
public interface InventoryClient {

    @PostMapping("/internal/events/{eventId}/seats")
    void provisionSeats(@PathVariable("eventId") UUID eventId,
                        @RequestBody ProvisionSeatsRequest request);
}