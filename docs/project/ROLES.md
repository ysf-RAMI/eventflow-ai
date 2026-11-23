# Team roles

Roles are ownership areas, not silos. Every member reviews at least one pull request per week outside their own area.

## Role 1 — Catalog, Inventory and Booking

- Owns: `catalog-service`, `inventory-service`, `booking-service`.
- Responsible for: Event CRUD, seat inventory, atomic holds, expiry, Booking state machine, REST Saga orchestration, concurrency tests.
- Key success measure: no double hold and no confirmed booking without successful simulation and Inventory confirmation.

## Role 2 — Payment simulation

- Owns: `payment-service`.
- Responsible for: simulated success/failure, payment persistence, one payment result per booking, idempotent REST behavior, tests.
- Out of scope: real provider integration, refunds, RabbitMQ, Outbox/Inbox.

## Role 3 — AI agent and RAG (later phase)

- Owns: the deferred AI Agent and MCP integration.
- Responsible for: Spring AI, RAG/pgvector, allowlisted tools, user-context propagation, AI evaluation.
- Key success measure: agent cannot confirm payment or access another customer's booking.

## Role 4 — Frontend, identity and DevOps

- Owns: `frontend/angular-app`, the planned API Gateway, Keycloak integration, `.github/workflows`, `docker-compose.yml`, infrastructure.
- Responsible for: Angular booking flow, JWT validation/routing, CI, containers, security baseline, observability and deployment.
- Notification Service is deferred and is not a current delivery responsibility.

## Shared responsibilities

- The project lead maintains the board and coordinates releases.
- Each service owner maintains its README, REST contract, migrations, and tests.
- Every pull request needs one approval from a reviewer outside the author's role.
