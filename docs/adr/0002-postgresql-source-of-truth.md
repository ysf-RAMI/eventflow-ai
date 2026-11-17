# ADR 0002: PostgreSQL is the source of truth

Status: Accepted — 17 November 2026.

## Context

Seat exclusivity, booking state, and simulated payment results must remain consistent. A cache must not decide whether a seat is available or a booking is confirmed.

## Decision

Each business service owns its PostgreSQL database. Inventory enforces seat concurrency using transactions, conditional updates/locking, and a partial unique index for active holds. Booking owns booking records and orchestrates the Saga. Payment owns one simulated payment record per booking. Redis is not required for the MVP.

## Consequences

- Correctness is enforced and tested in the owning database.
- Cross-service relationships are UUID references only; no cross-database foreign keys or JPA associations.
- A service never writes directly to another service's database.
