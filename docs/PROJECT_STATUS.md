# Project status — architecture handoff

## Scope of this handoff

This branch contains the corrected architecture, database, and core domain/persistence baseline for EventFlow AI. It is not a claim that every REST adapter or end-to-end workflow is implemented or executable.

## Approved MVP decisions

- Catalog, Inventory, Booking, and Payment are separate services.
- Each core service owns one PostgreSQL database.
- Inter-service communication is synchronous REST.
- Booking orchestrates the reservation workflow.
- Payment is a simulation only; no real provider and no refund workflow.
- Notification is deferred.
- RabbitMQ, Kafka, Outbox, Inbox, and AsyncAPI are excluded from the MVP.
- Keycloak owns users, credentials, and `CUSTOMER` / `ADMIN` roles.
- DTOs are Java records; JPA entities are regular classes.
- REST controllers and MCP tools are separate; MCP is a later AI integration.
- GraphQL is future scope.
- No Customer/Admin/Role JPA entities and no cross-service JPA relationships.

## Current repository baseline

- Inventory has the Seat and SeatHold domain models, repositories, migrations, and PostgreSQL configuration.
- Catalog has the Event domain model, repository, application skeleton, migration, and PostgreSQL configuration.
- Booking has the Booking and BookingItem domain models, repositories, application skeleton, migration, and PostgreSQL configuration.
- Payment has the Payment domain model, repository, application skeleton, migration, and PostgreSQL configuration.
- API Gateway, Angular, AI Agent, and Notification are planned/deferred components.

## Tests and verification

No new tests are created as part of this handoff. No tests, Maven build, Docker startup, Flyway execution, or end-to-end flow are run as part of this handoff. Existing test files are preserved unchanged. Do not describe the repository as fully compiled, tested, or production-ready.

Testing and runtime verification belong to a later, explicitly approved QA phase.

## Next implementation phase

1. Review the UML `.mmd` files and `docs/classes/CLASS_DESIGN.md`.
2. Compare each SQL migration with its class design.
3. Implement one service at a time.
4. Keep service ownership and REST contracts aligned.
5. Do not add deferred technologies or features without an explicit architecture decision.
