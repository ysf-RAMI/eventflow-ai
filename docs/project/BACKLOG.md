# Task backlog

Create one GitHub issue per row, with the task ID in the title and a label for the role. Tasks in later phases depend on earlier ones. Within a phase, tasks with the same owner can run in parallel.

Status: T-001, T-002, and T-020 are done in the first commit.

## Phase 0 and 1: Setup and requirements

| ID | Task | Owner | Depends on | Done when |
|---|---|---|---|---|
| T-001 | Repo, branches, README, CI | Role 4 | none | `develop` exists, CI green on a clean clone |
| T-002 | Docker compose for inventory DB and RabbitMQ | Role 4 | T-001 | `docker compose up -d` starts both, DB healthy |
| T-003 | Review and approve SRS draft | All | none | Every FR and NFR has an owner and no open objections |
| T-004 | Review use case and class diagrams | All | T-003 | Team sign-off recorded in the SRS |

## Phase 2: Conception

| ID | Task | Owner | Depends on | Done when |
|---|---|---|---|---|
| T-010 | Review and accept ADR 0001 to 0003 | All | T-003 | Status changed from Proposed to Accepted |
| T-011 | C4 container diagram in docs/architecture | Role 1 | T-010 | Diagram matches the service list in README |
| T-012 | OpenAPI contract for inventory service | Role 1 | T-011 | `docs/api-contracts/inventory.yaml` validates |
| T-013 | OpenAPI contract for booking service | Role 1 | T-011 | `docs/api-contracts/booking.yaml` validates |
| T-014 | Merise MCD, MLD, MPD for inventory and booking | Role 1 | T-011 | Documents in docs/merise, reviewed |
| T-015 | AsyncAPI contract for events (PaymentRequested, PaymentSucceeded, PaymentFailed, BookingConfirmed, BookingCancelled) | Role 2 | T-011 | `docs/api-contracts/events.yaml` validates |
| T-016 | Merise MCD, MLD, MPD for payment | Role 2 | T-015 | Documents in docs/merise, reviewed |
| T-017 | Agent tool contract (names, parameters, authorization rules, confirmation rules) | Role 3 | T-012, T-013 | `docs/api-contracts/agent-tools.md` reviewed |

## Phase 3, Increment A: Core booking (Nov 7 to 15)

| ID | Task | Owner | Depends on | Done when |
|---|---|---|---|---|
| T-020 | Inventory service skeleton: Seat entity, V1 migration, health | Role 1 | T-002 | Done in first commit |
| T-021 | V2 migration: seat_hold table with one active hold per seat | Role 1 | T-020 | Partial unique index enforces one ACTIVE hold per seat |
| T-022 | Atomic seat hold endpoint (conditional update, row locking) | Role 1 | T-021 | Integration test: 100 concurrent requests for one seat yield exactly 1 hold |
| T-023 | Hold expiry job (scheduled release of expired holds) | Role 1 | T-022 | Expired holds release their seats, tested with a fixed clock |
| T-024 | Booking service skeleton with its own DB (booking_db, port 5434) | Role 1 | T-013 | Starts, health UP, Flyway V1 applied |
| T-025 | Booking state machine (Pending, Held, Confirmed, Failed, Expired, Cancelled, Refunded) | Role 1 | T-024 | Unit tests cover every allowed transition and reject the rest |
| T-026 | Create booking from held seats; catalog read endpoints | Role 1 | T-022, T-025 | OpenAPI contract tests pass |

## Phase 3, Increment B: Transactions (Nov 16 to 24)

| ID | Task | Owner | Depends on | Done when |
|---|---|---|---|---|
| T-030 | RabbitMQ configuration: exchanges, queues, dead-letter queues | Role 2 | T-002 | Messages route and dead-letter as configured |
| T-031 | Payment service with simulator: success, decline, timeout, delayed confirmation, duplicate callbacks | Role 2 | T-016, T-030 | Each simulator mode has a test |
| T-032 | Idempotency key on payments, unique constraint | Role 2 | T-031 | Repeating a payment request creates one charge |
| T-033 | Transactional outbox in booking and payment | Role 1, Role 2 | T-026, T-032 | Stopping the broker after DB commit does not lose the event |
| T-034 | Saga compensation: payment succeeded but hold expired leads to refund and release | Role 1, Role 2 | T-033 | Fault injection test passes |
| T-035 | Timeouts, bounded retries, circuit breaker on booking to payment and booking to inventory | Role 2 | T-031 | Stopping payment service fails fast and the booking ends in a consistent state |
| T-036 | Notification service: consume events, send Telegram messages | Role 4 | T-015 | Booking confirmed and payment failed messages delivered to a test chat |
| T-037 | Inbox pattern and idempotent consumers in notification and booking | Role 2 | T-033 | Same event delivered twice produces one side effect |

## Phase 3, Increment C: AI agent (Nov 25 to Dec 2)

| ID | Task | Owner | Depends on | Done when |
|---|---|---|---|---|
| T-040 | Spring AI setup, ChatClient, LLM provider config (local Ollama or hosted API) | Role 3 | T-017 | A chat request returns an answer, config in application.yml |
| T-041 | RAG ingestion: policy PDFs, chunking, embeddings, pgvector | Role 3 | T-040 | Policy question returns the correct policy with citation |
| T-042 | Tool calling: searchEvents, getSeatAvailability, createReservationHold, getBookingStatus | Role 3 | T-026, T-040 | Each tool called correctly in tests with a mocked LLM |
| T-043 | Confirmation gate for payment and cancellation; agent cannot call them directly | Role 3 | T-042 | Cancel without confirmation is rejected in test |
| T-044 | Authorization: agent acts under the authenticated customer's identity | Role 3, Role 4 | T-043, T-055 | An agent request for another customer's booking is refused |
| T-045 | AI evaluation dataset: 30 to 50 scenarios, scoring script | Role 3 | T-042 | Dataset committed, script reports retrieval, correctness, tool-selection scores |

## Phase 3, Increment D: Frontend and DevOps (Dec 3 to 7)

| ID | Task | Owner | Depends on | Done when |
|---|---|---|---|---|
| T-050 | Angular app skeleton, event list page | Role 4 | T-026 | Lists events from catalog endpoint |
| T-051 | Seat map, hold, and payment step in the UI | Role 4 | T-050, T-033 | Customer completes a booking end to end in the browser |
| T-052 | Telegram bot: booking status and cancellation request commands | Role 4 | T-036, T-043 | Commands work against the test environment |
| T-053 | Dockerfile per service and full-stack docker-compose | Role 4 | T-024, T-031 | `docker compose up` starts every service, all health UP |
| T-054 | CI for every service, image build and scan (Trivy) | Role 4 | T-053 | CI green on develop for all services |
| T-055 | API gateway with JWT authentication | Role 4 | T-053 | Requests without a valid token get 401 |
| T-056 | Deploy to AWS EC2 with K3s, HTTPS ingress | Role 4 | T-054 | Public URL serves the app with TLS |

## Phase 4 and 5: QA, observability, delivery (Dec 8 to 13)

| ID | Task | Owner | Depends on | Done when |
|---|---|---|---|---|
| T-060 | Testcontainers integration tests for every service | All | Increments A to D | Tests run in CI |
| T-061 | k6 scripts: concurrent seat holds, load on booking flow | Role 1 | T-022 | Report shows 1 successful hold for 100 concurrent requests |
| T-062 | Fault experiments: stop payment, stop RabbitMQ, duplicate events | Role 2 | T-035, T-037 | Results documented with before and after state |
| T-063 | Actuator, Prometheus, Grafana dashboard, Loki logs, Tempo traces | Role 4 | T-053 | One dashboard shows metrics, logs, and a booking trace |
| T-064 | OWASP ZAP scan and fixes for high findings | Role 4 | T-055 | No open high-severity findings |
| T-065 | Playwright end-to-end test: customer books and pays | Role 4 | T-051 | Test passes in CI |
| T-066 | Final report, architecture diagrams, README runbook | All | all | Report reviewed, README reproduces the demo |
| T-067 | 5-minute demo rehearsal and presentation | All | T-066 | Rehearsed twice, under 5 minutes |
