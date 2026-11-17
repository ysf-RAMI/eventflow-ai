# Task backlog — EventFlow AI REST-only MVP

Create GitHub issues from these tasks and keep the task ID in each title. The current implementation pass is not a QA pass: do not create or run tests now. Testing tasks are explicitly deferred to a later phase.

## Phase 0 — Architecture and repository alignment

| ID | Task | Owner | Depends on | Done when |
|---|---|---|---|---|
| T-001 | Align repository README and project documentation | Role 4 | none | README, SRS, architecture, database, UML and class design agree |
| T-002 | Docker Compose for four core PostgreSQL databases | Role 4 | T-001 | Four database services and unique host ports are documented |
| T-003 | Approve SRS and service boundaries | All | T-001 | Scope matches the approved REST-only MVP |
| T-004 | Approve UML and class design | All | T-003 | No cross-service JPA or SQL relationships are modeled |

## Phase 1 — Catalog Service

| ID | Task | Owner | Depends on | Done when |
|---|---|---|---|---|
| T-010 | Catalog Spring Boot skeleton and catalog_db | Role 1 | T-002 | Application structure and Flyway baseline are present |
| T-011 | Event entity and EventStatus | Role 1 | T-010 | Event has id, createdBy, title, description, date, status only |
| T-012 | Event REST CRUD and publish operations | Role 1 | T-011 | REST contract and service responsibilities are implemented |
| T-013 | Read-only Catalog MCP tools (later AI phase) | Role 3 | T-012 | Tools call the same application service and are read-only |

## Phase 2 — Inventory Service

| ID | Task | Owner | Depends on | Done when |
|---|---|---|---|---|
| T-020 | Preserve Inventory Seat skeleton and V1 migration | Role 1 | T-002 | Existing source remains compatible with the agreed model |
| T-021 | SeatHold entity, HoldStatus and V2 migration | Role 1 | T-020 | One ACTIVE hold per seat is enforced by the database |
| T-022 | Atomic multi-seat hold, confirm and release REST endpoints | Role 1 | T-021 | All-or-nothing hold behavior is implemented |
| T-023 | Hold expiry mechanism | Role 1 | T-022 | Expired holds are marked EXPIRED and seats return to AVAILABLE |
| T-024 | Read-only Inventory MCP tools (later AI phase) | Role 3 | T-022 | MCP tools cannot create/confirm/release holds |

## Phase 3 — Booking Service

| ID | Task | Owner | Depends on | Done when |
|---|---|---|---|---|
| T-030 | Booking Spring Boot skeleton, booking_db and schema | Role 1 | T-002 | Application structure and migration baseline are present |
| T-031 | Booking and BookingItem entities, repositories and status transitions | Role 1 | T-030 | Domain model matches UML and SQL |
| T-032 | Inventory REST client and BookingSagaOrchestrator | Role 1 | T-022, T-031 | Create booking → hold → save items flow is implemented |
| T-033 | Booking REST endpoints and ownership checks | Role 1 | T-032 | Customer identity comes from Keycloak JWT subject |
| T-034 | Release hold on payment failure, expiry or unpaid cancellation | Role 1 | T-032 | Compensation behavior is implemented |
| T-035 | Booking MCP tools (later AI phase) | Role 3 | T-033 | Tools enforce customer identity and cannot pay |

## Phase 4 — Payment simulation

| ID | Task | Owner | Depends on | Done when |
|---|---|---|---|---|
| T-040 | Payment Spring Boot skeleton, payment_db and schema | Role 2 | T-002 | Application structure and migration baseline are present |
| T-041 | Payment simulator REST endpoint | Role 2 | T-040 | Server-side simulation returns SUCCEEDED or FAILED |
| T-042 | One payment per booking | Role 2 | T-041 | Unique booking_id and repeated request returns existing result |
| T-043 | Booking simulated-payment flow | Role 1 | T-041, T-032 | Explicit customer confirmation → simulation → Inventory confirm |

## Phase 5 — Identity and frontend

| ID | Task | Owner | Depends on | Done when |
|---|---|---|---|---|
| T-050 | Keycloak roles and API Gateway JWT integration | Role 4 | T-012, T-033 | CUSTOMER/ADMIN permissions are integrated |
| T-051 | Angular event list and details | Role 4 | T-012 | Events load from Catalog |
| T-052 | Angular availability, booking and simulated payment flow | Role 4 | T-043, T-051 | Customer flow is implemented |

## Deferred phase — AI and notifications

- AI Agent Service, Spring AI, RAG, pgvector and restricted MCP tools.
- Notification Service and Telegram.
- GraphQL.

## Deferred QA phase — not part of the current task

- Unit and integration tests for each service.
- 100 concurrent hold requests for one seat.
- All-or-nothing multi-seat hold tests.
- Payment simulation idempotency tests.
- Hold expiry and Saga compensation tests.
- End-to-end browser tests.
