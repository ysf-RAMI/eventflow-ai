# Database model and ownership

## 1. Database inventory

| Service | Database | Host port | Tables | Owner |
|---|---|---:|---|---|
| Catalog | `catalog_db` | 5432 | `events` | Catalog Service |
| Inventory | `inventory_db` | 5433 | `event_seat`, `seat_hold` | Inventory Service |
| Booking | `booking_db` | 5434 | `booking`, `booking_item` | Booking Service |
| Payment | `payment_db` | 5435 | `payment` | Payment Service |
| AI Agent (later) | `ai_db` | 5436 (future) | `policy_document`, `document_chunk` | AI Agent Service |

All five databases are defined in the current `docker-compose.yml`. `ai_db` uses the `pgvector/pgvector:pg16` image and currently holds only the `vector` extension; the `policy_document` and `document_chunk` tables are created in the RAG task (T-065).

## 2. Table definitions

### Catalog — `events`

- `id UUID PRIMARY KEY`
- `created_by UUID NOT NULL` — Keycloak subject of the admin who created the event
- `title VARCHAR(200) NOT NULL`
- `description TEXT`
- `event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL` — Java property `date` (`LocalDateTime`)
- `status VARCHAR(20) NOT NULL` — `DRAFT`, `PUBLISHED`, `CANCELLED`, `COMPLETED`

No venue, venue address, starts/ends range, cancellation policy, or created/updated/published timestamps in this approved Event model.

### Inventory — `event_seat`

- `id UUID PRIMARY KEY`
- `event_id UUID NOT NULL` — external Catalog ID
- `label VARCHAR(10) NOT NULL`
- `section VARCHAR(50)`
- `price_cents BIGINT NOT NULL CHECK (price_cents >= 0)`
- `status VARCHAR(20) NOT NULL` — `AVAILABLE`, `HELD`, `SOLD`
- `version BIGINT NOT NULL` — JPA optimistic-lock version
- Unique `(event_id, label)`

### Inventory — `seat_hold`

- `id UUID PRIMARY KEY`
- `seat_id UUID NOT NULL REFERENCES event_seat(id)`
- `booking_id UUID NOT NULL` — external Booking ID
- `expires_at TIMESTAMPTZ NOT NULL` — authoritative expiry
- `status VARCHAR(20) NOT NULL` — `ACTIVE`, `CONFIRMED`, `RELEASED`, `EXPIRED`
- `created_at TIMESTAMPTZ NOT NULL`
- Partial unique index on `seat_id WHERE status = 'ACTIVE'`

### Booking — `booking`

- `id UUID PRIMARY KEY`
- `customer_id UUID NOT NULL` — Keycloak subject
- `event_id UUID NOT NULL` — external Catalog ID
- `status VARCHAR(20) NOT NULL` — `PENDING`, `HELD`, `CONFIRMED`, `FAILED`, `EXPIRED`, `CANCELLED`
- `total_cents BIGINT` — nullable until Inventory returns held seat prices
- `hold_expires_at TIMESTAMPTZ` — snapshot returned by Inventory
- `created_at TIMESTAMPTZ NOT NULL`

### Booking — `booking_item`

- `id UUID PRIMARY KEY`
- `booking_id UUID NOT NULL REFERENCES booking(id) ON DELETE CASCADE`
- `seat_id UUID NOT NULL` — external Inventory ID
- `price_cents BIGINT NOT NULL CHECK (price_cents >= 0)` — price snapshot
- Unique `(booking_id, seat_id)`

### Payment — `payment`

- `id UUID PRIMARY KEY`
- `booking_id UUID NOT NULL UNIQUE` — external Booking ID; one simulation record per booking
- `amount_cents BIGINT NOT NULL CHECK (amount_cents >= 0)`
- `status VARCHAR(20) NOT NULL` — `PENDING`, `SUCCEEDED`, `FAILED`
- `created_at TIMESTAMPTZ NOT NULL`

There is no real payment provider, refund table, or cross-database foreign key.

## 3. Relations

- Catalog `events.id` → Inventory `event_seat.event_id`: logical reference only.
- Catalog `events.id` → Booking `booking.event_id`: logical reference only.
- Inventory `event_seat.id` → Inventory `seat_hold.seat_id`: local SQL foreign key.
- Booking `booking.id` → Booking `booking_item.booking_id`: local SQL foreign key.
- Inventory `event_seat.id` → Booking `booking_item.seat_id`: logical reference only.
- Booking `booking.id` → Inventory `seat_hold.booking_id`: logical reference only.
- Booking `booking.id` → Payment `payment.booking_id`: logical reference only, unique in Payment.

## 4. Migration locations

- Catalog: `catalog-service/src/main/resources/db/migration/`
- Inventory: `inventory-service/src/main/resources/db/migration/`
- Booking: `booking-service/src/main/resources/db/migration/`
- Payment: `payment-service/src/main/resources/db/migration/`

The four core services have matching Spring Boot application configuration and JPA domain/persistence baselines. REST adapters and application orchestration are implemented in a later phase; migrations remain the database source of truth.
