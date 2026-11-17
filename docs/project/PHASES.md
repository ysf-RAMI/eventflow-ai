# Project phases — EventFlow AI

This is a dependency-based plan. The current task is architecture alignment and service implementation scaffolding. Automated testing is explicitly deferred; do not create or run tests in the current implementation pass.

## Phase 0 — Repository and local infrastructure

- Align README, SRS, architecture, database model, REST/MCP contracts, UML and class design.
- Preserve existing source files and existing tests.
- Keep Docker Compose limited to the four core PostgreSQL databases.
- Do not add RabbitMQ/Kafka or notification infrastructure.
- Definition of Done: documentation and service ownership agree; database service names and ports are documented.

## Phase 1 — Catalog Service

1. Create the Spring Boot application skeleton.
2. Implement `Event`, `EventStatus`, repository, service and REST controller.
3. Add read-only MCP tools as a later adapter when the AI integration is started.
4. Use `date` as the single event date/time field.
5. Keep DTOs as Java records.

## Phase 2 — Inventory Service

1. Preserve the existing `Seat` implementation.
2. Complete `SeatHold` and `HoldStatus`.
3. Implement seat management and availability REST endpoints.
4. Implement atomic multi-seat hold, confirmation, release and expiry.
5. Add read-only MCP availability tools only in the AI integration phase.

## Phase 3 — Booking Service

1. Implement `Booking`, `BookingItem`, and `BookingStatus`.
2. Implement repositories and local business rules.
3. Implement `InventoryClient` and `PaymentClient`.
4. Implement `BookingSagaOrchestrator` with REST calls and compensation.
5. Implement REST endpoints and ownership checks.
6. Add MCP tools only in the AI integration phase.

## Phase 4 — Payment simulation

1. Implement `Payment` and `PaymentStatus`.
2. Implement one simulated payment record per booking.
3. Expose internal REST endpoints.
4. Do not integrate a real payment provider or implement refunds.

## Phase 5 — Identity and frontend

- Integrate Keycloak roles (`CUSTOMER`, `ADMIN`) and JWT validation.
- Implement API Gateway routing.
- Implement Angular event discovery, seat selection, booking and simulated payment.

## Deferred phases

- AI Agent Service, Spring AI, RAG, pgvector and restricted MCP tools.
- Notification Service and Telegram.
- GraphQL.
- Automated unit, integration, concurrency and end-to-end tests.
- Real payment provider, refunds and confirmed-booking cancellation policy.
- RabbitMQ/Kafka, Outbox/Inbox, Redis, Kubernetes and cloud deployment.
