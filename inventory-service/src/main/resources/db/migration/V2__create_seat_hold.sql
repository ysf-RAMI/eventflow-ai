CREATE TABLE seat_hold (
    id         UUID PRIMARY KEY,
    seat_id    UUID        NOT NULL REFERENCES event_seat(id),
    booking_id UUID        NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    status     VARCHAR(20) NOT NULL
               CHECK (status IN ('ACTIVE', 'CONFIRMED', 'RELEASED', 'EXPIRED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_seat_hold_one_active_per_seat
    ON seat_hold (seat_id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_seat_hold_booking ON seat_hold (booking_id);
CREATE INDEX idx_seat_hold_expiry ON seat_hold (expires_at)
    WHERE status = 'ACTIVE';
