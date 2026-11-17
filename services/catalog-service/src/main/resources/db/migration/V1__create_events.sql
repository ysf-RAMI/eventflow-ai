CREATE TABLE events (
    id          UUID PRIMARY KEY,
    created_by  UUID         NOT NULL,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    event_date  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'DRAFT'
                CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED'))
);

CREATE INDEX idx_events_date ON events (event_date);
CREATE INDEX idx_events_status_date ON events (status, event_date);
