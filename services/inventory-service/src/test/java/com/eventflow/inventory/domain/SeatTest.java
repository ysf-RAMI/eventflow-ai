package com.eventflow.inventory.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeatTest {

    private Seat newSeat() {
        return Seat.create(UUID.randomUUID(), "A12", "A", 25000);
    }

    @Test
    void newSeatIsAvailable() {
        assertThat(newSeat().getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void availableSeatCanBeHeld() {
        Seat seat = newSeat();
        seat.hold();
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);
    }

    @Test
    void heldSeatCannotBeHeldAgain() {
        Seat seat = newSeat();
        seat.hold();
        assertThatThrownBy(seat::hold).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void heldSeatCanBeSold() {
        Seat seat = newSeat();
        seat.hold();
        seat.sell();
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.SOLD);
    }

    @Test
    void soldSeatCannotBeReleased() {
        Seat seat = newSeat();
        seat.hold();
        seat.sell();
        assertThatThrownBy(seat::release).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void negativePriceIsRejected() {
        assertThatThrownBy(() -> Seat.create(UUID.randomUUID(), "A1", "A", -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
