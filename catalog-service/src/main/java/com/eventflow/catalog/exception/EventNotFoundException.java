package com.eventflow.catalog.exception;

import java.util.UUID;

public class EventNotFoundException extends RuntimeException{
    public EventNotFoundException(UUID eventId){
        super("event not found " +eventId);
    }
}
