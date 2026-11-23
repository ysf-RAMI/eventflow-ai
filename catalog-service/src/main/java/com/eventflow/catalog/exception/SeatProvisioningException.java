package com.eventflow.catalog.exception;

import java.util.UUID;

public class SeatProvisioningException extends RuntimeException {

    public SeatProvisioningException(UUID eventId, Throwable cause) {
        super("Seat provisioning failed for event " + eventId, cause);
    }
}