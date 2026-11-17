CREATE TABLE booking (
    id              UUID PRIMARY KEY,
    customer_id     UUID        NOT NULL,
    event_id        UUID        NOT NULL,
    status          VARCHAR(20) NOT NULL
                    CHECK (status IN ('PENDING', 'HELD', 'CONFIRMED', 'FAILED', 'EXPIRED', 'CANCELLED')),
    total_cents     BIGINT CHECK (total_cents IS NULL OR total_cents >= 0),
    hold_expires_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_booking_customer_created ON booking (customer_id, created_at DESC);
CREATE INDEX idx_booking_event ON booking (event_id);

CREATE TABLE booking_item (
    id          UUID PRIMARY KEY,
    booking_id  UUID   NOT NULL REFERENCES booking(id) ON DELETE CASCADE,
    seat_id     UUID   NOT NULL,
    price_cents BIGINT NOT NULL CHECK (price_cents >= 0),
    CONSTRAINT uk_booking_item_booking_seat UNIQUE (booking_id, seat_id)
);

CREATE INDEX idx_booking_item_seat ON booking_item (seat_id);
