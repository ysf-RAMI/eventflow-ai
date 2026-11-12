CREATE TABLE event_seat (
    id          UUID PRIMARY KEY,
    event_id    UUID        NOT NULL,
    label       VARCHAR(10) NOT NULL,
    section     VARCHAR(50),
    price_cents BIGINT      NOT NULL CHECK (price_cents >= 0),
    status      VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
                CHECK (status IN ('AVAILABLE', 'HELD', 'SOLD')),
    version     BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uk_event_seat_label UNIQUE (event_id, label)
);

CREATE INDEX idx_event_seat_event ON event_seat (event_id);
