CREATE TABLE booking
(
    id              UUID                        NOT NULL,
    customer_id     UUID                        NOT NULL,
    event_id        UUID                        NOT NULL,
    inventory_hold_id UUID,
    status          VARCHAR(20)                 NOT NULL,
    total_cents     BIGINT,
    hold_expires_at TIMESTAMP(6) WITH TIME ZONE,
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_booking PRIMARY KEY (id)
);

CREATE TABLE booking_item
(
    id          UUID   NOT NULL,
    booking_id  UUID   NOT NULL,
    seat_id     UUID   NOT NULL,
    price_cents BIGINT NOT NULL,
    CONSTRAINT pk_booking_item PRIMARY KEY (id)
);