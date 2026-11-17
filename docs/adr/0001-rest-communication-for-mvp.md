# ADR 0001: Synchronous REST communication for the MVP

Status: Accepted — 17 November 2026.

## Context

The MVP needs a straightforward reservation flow that is easy to develop, debug, and demonstrate. The team has decided not to implement a message broker.

## Decision

Use synchronous REST calls between Catalog, Inventory, Booking, and Payment for the MVP. Booking owns the Saga orchestration. No RabbitMQ, Kafka, Outbox, Inbox, AsyncAPI, or asynchronous event delivery is implemented.

## Consequences

- Fewer infrastructure components and simpler local setup.
- Service calls need bounded timeouts, explicit error handling, and idempotent operations where retries are possible.
- A Saga consists of local database transactions and compensating REST calls; it is not a distributed ACID transaction.
- Asynchronous messaging can be reconsidered only as a future architecture change, not assumed by current requirements.
