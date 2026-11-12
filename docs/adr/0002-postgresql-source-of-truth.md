# ADR 0002: PostgreSQL is the source of truth; Redis only caches

Status: Proposed (T-010)

## Context
Seat exclusivity, booking state, and payments must never be inconsistent. A cache cannot guarantee this on its own.

## Decision
Seats, holds, bookings, and payments live in PostgreSQL, one database per service. Concurrency is enforced with atomic conditional updates, row-level locking, and unique constraints. Redis is used only for caching and rate limiting.

## Consequences
- Correctness is testable in the database with concurrency tests.
- Redis is optional for the MVP and can be added without changing correctness.
