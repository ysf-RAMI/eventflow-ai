ALTER TABLE booking
    ADD seats_number INT;

ALTER TABLE booking
    ALTER COLUMN seats_number SET NOT NULL;

ALTER TABLE booking
    DROP COLUMN status;

ALTER TABLE booking
    ADD status VARCHAR(20) NOT NULL;