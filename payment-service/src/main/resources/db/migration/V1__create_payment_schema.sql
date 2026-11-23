CREATE TABLE payment (
    id          UUID PRIMARY KEY,
    booking_id  UUID        NOT NULL UNIQUE,
    amount_cents BIGINT     NOT NULL CHECK (amount_cents >= 0),
    status      VARCHAR(20) NOT NULL
                CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
