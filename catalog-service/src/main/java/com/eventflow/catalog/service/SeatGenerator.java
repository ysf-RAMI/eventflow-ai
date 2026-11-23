package com.eventflow.catalog.service;

import com.eventflow.catalog.dto.SeatSpec;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class SeatGenerator {

    static final int MIN_SEATS = 70;
    static final int MAX_SEATS = 100;
    static final int MIN_PRICE_UNITS = 100;
    static final int MAX_PRICE_UNITS = 1000;
    static final int SEATS_PER_ROW = 10;
    static final String SECTION = "GENERAL";

    public List<SeatSpec> generate(UUID eventId) {
        Random random = new Random(
                eventId.getMostSignificantBits() * 31 + eventId.getLeastSignificantBits());
        int count = MIN_SEATS + random.nextInt(MAX_SEATS - MIN_SEATS + 1);
        List<SeatSpec> seats = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            char row = (char) ('A' + i / SEATS_PER_ROW);
            int number = i % SEATS_PER_ROW + 1;
            int units = MIN_PRICE_UNITS + random.nextInt(MAX_PRICE_UNITS - MIN_PRICE_UNITS + 1);
            seats.add(new SeatSpec(String.valueOf(row) + number, SECTION, units * 100L));
        }
        return seats;
    }
}