# EventFlow AI

EventFlow AI is a distributed event-reservation platform with an AI booking assistant planned for a later phase. The MVP focuses on event management, seat availability, temporary seat holds, booking orchestration, and a simulated payment flow.

> **Architecture baseline:** Java 21, Spring Boot 3.3 (Catalog and Booking use 4.1), PostgreSQL 16, REST between services, Keycloak for identity, and MCP tools for authorized AI access when the AI phase is implemented. RabbitMQ is not used in this project. Notifications are deferred.

## 1. Services and current status

| Component | Port | Database | Scope / status |
|---|---:|---|---|
| API Gateway | 8080 | none | Planned; JWT validation and routing |
| Catalog Service | 8081 | `catalog_db` (5432) | Spring Boot domain/persistence baseline |
| Inventory Service | 8082 | `inventory_db` (5433) | Existing Seat skeleton; SeatHold design and migration added |
| Booking Service | 8083 | `booking_db` (5434) | Architecture baseline; local implementation available in `booking-service/` |
| Payment Service | 8084 | `payment_db` (5435) | Simulation REST endpoints and persistence |
| AI Agent Service | 8085 | `ai_db` (5436, future) | Later phase: RAG and controlled MCP tools |
| Notification Service | 8086 | none initially | Deferred; no notification implementation in MVP |
| Angular frontend | 4200 | none | Planned |

Each business service owns its database. Cross-service identifiers are UUID values; there are no cross-database foreign keys or JPA relationships.

## 2. MVP workflow

1. An authenticated customer submits an event and seat selection to Booking.
2. Booking creates a `PENDING` booking and calls Inventory over REST.
3. Inventory atomically holds all requested seats for 10 minutes and returns the prices and expiry time.
4. Booking stores one `BookingItem` per seat, the total, and a copy of the hold expiry (`hold_expires_at`), then changes to `HELD`.
5. The customer explicitly confirms the simulated payment.
6. Booking calls Payment over REST. Payment records one deterministic simulation result per booking.
7. If the simulation succeeds, Booking asks Inventory to confirm the hold. Only then does Booking become `CONFIRMED`.
8. If payment fails or the hold cannot be confirmed, Booking fails and Inventory releases any still-active hold.

No real money is collected. A simulated success is not a bank charge.

## 3. Quick start: databases

Prerequisites: Docker Desktop / Docker Engine and Docker Compose.

```bash
docker compose up -d
```

This starts the four PostgreSQL databases for the core services. It does **not** start application services, Keycloak, RabbitMQ, the AI stack, or notifications.

The existing Inventory application skeleton can be started (requires JDK 21 and Maven):

```bash
cd inventory-service
mvn spring-boot:run
```

Health check: http://localhost:8082/actuator/health

Development database credentials are intentionally simple and must be replaced outside local development. Do not commit real secrets.

## 4. Project structure

```text
docs/
  adr/                 Architecture decision records
  api-contracts/       REST and MCP interface baseline
  architecture/        C4 view, state machine, sequence
  database/            Database ownership and schema overview
  project/             Phases, roles, backlog
  requirements/        SRS
  uml/                 Separate Mermaid UML source files (.mmd)
  classes/             Per-service Java class responsibilities and package layout
catalog-service/       Catalog implementation
inventory-service/     Inventory implementation
booking-service/       Booking implementation (H2, REST, Swagger)
payment-service/       Payment simulator implementation
frontend/angular-app/  Planned Angular client
```

### Local Booking service quick start

The active local implementation is at the repository root in `booking-service/`:

```bash
cd booking-service
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run`.

- API base URL: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs
- H2 console: http://localhost:8081/h2-console

The local service uses a file-backed H2 database at `./data/bookingdb`, Spring profiles,
and temporary local Inventory and Payment clients. The production target remains
PostgreSQL, separate Booking/Inventory/Payment services, and REST communication.

## 5. Design rules

- Keep each service responsible for its own data and business rules.
- Use REST for synchronous service-to-service communication in the MVP.
- Keep DTOs as Java `record` types and JPA entities as regular classes.
- Keep REST endpoints and MCP tools separate; MCP tools are not ordinary `@RestController` methods.
- Catalog, Inventory, and Booking are planned to expose REST plus restricted MCP tools. Payment exposes REST only.
- GraphQL is a future extension, not part of the MVP.
- No RabbitMQ, Kafka, Outbox, Inbox, or AsyncAPI in this MVP.
- Keycloak owns user identities and roles (`CUSTOMER`, `ADMIN`); there is no local `Customer` or `Admin` JPA entity.
- Inventory is the source of truth for seat holds and their real expiry. Booking may store `hold_expires_at` as a read/display snapshot.
- Notification and AI/RAG implementation are later phases.

## 6. Documentation

- [Architecture](docs/architecture/README.md)
- [Requirements / SRS](docs/requirements/SRS.md)
- [UML diagrams](docs/uml/README.md)
- [Java class design](docs/classes/CLASS_DESIGN.md)
- [Database model](docs/database/README.md)
- [REST interface baseline](docs/api-contracts/REST.md)
- [MCP tool baseline](docs/api-contracts/MCP.md)
- [Project phases](docs/project/PHASES.md)
- [Backlog](docs/project/BACKLOG.md)
- [Team roles](docs/project/ROLES.md)
- [Architecture decisions](docs/adr/)

## 7. Current implementation boundary

The repository architecture includes domain entities, repositories, migrations, and application
configuration baselines for the core services. The Payment simulator is also implemented.

The root `booking-service/` is the active local Booking implementation. It provides:

- REST endpoints for creating, listing, retrieving, filtering, and cancelling bookings.
- Swagger/OpenAPI documentation.
- UUID-based Booking and BookingItem persistence.
- A local 15-minute Inventory hold simulation.
- Simulated payment followed by hold confirmation or release.
- No cross-service JPA relationships.

Keycloak enforcement, production REST clients, PostgreSQL/Flyway runtime configuration, and
end-to-end orchestration remain for the next implementation phase. Notification and AI Agent
implementation are deferred. Existing test files are preserved unchanged.

See [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) for the current status and [AGENT_PROMPT.md](AGENT_PROMPT.md) for the repository-update instructions.

## 8. Git workflow

- `master`: current integration branch.
- `feature/<task-id>-short-name`: feature work.
- Use Conventional Commits and reference the task ID, e.g. `feat(inventory): T-021 add seat hold model`.
