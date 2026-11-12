# EventFlow AI

A distributed event-reservation platform with an AI booking agent (RAG + tool calling). It is the project for the Distributed Systems module (Systèmes Répartis et Distribués).

The core engineering problems are concurrent seat booking, consistent reservation and payment through Saga compensation, and an AI agent that can act only through controlled, authorized tools.

## Services

| Service | Port | Database | Status |
|---|---|---|---|
| api-gateway | 8080 | none | Planned (T-055) |
| inventory-service | 8082 | inventory_db (5433) | Skeleton: Seat entity, V1 migration, health |
| booking-service | 8083 | booking_db (5434) | Planned (T-024) |
| payment-service | 8084 | payment_db (5435) | Planned (T-031) |
| ai-agent-service | 8085 | ai_db with pgvector (5436) | Planned (T-040) |
| notification-service | 8086 | none | Planned (T-036) |
| frontend/angular-app | 4200 | none | Planned (T-050) |

## Quick start (Windows, PowerShell)

Prerequisites: JDK 21, Maven, Git, Docker Desktop (running).

```
docker compose up -d
cd services\inventory-service
mvn spring-boot:run
```

Then open http://localhost:8082/actuator/health. You should see `{"status":"UP"}`.

## Documentation

- Project: [phases](docs/project/PHASES.md), [roles](docs/project/ROLES.md), [task backlog](docs/project/BACKLOG.md)
- Requirements: [SRS](docs/requirements/SRS.md)
- Design: [use cases and class diagrams](docs/uml/README.md), [architecture, state machine and sequences](docs/architecture/README.md)
- Decisions: [ADRs](docs/adr/)

## Branching and commits

- `main`: stable, release-ready code only.
- `develop`: integration branch. All feature work merges here.
- `feature/<task-id>-short-name`, for example `feature/T-022-atomic-seat-hold`. Open a pull request into `develop` and get one approval before merging.
- Commits follow Conventional Commits and cite the task ID: `feat(inventory): T-021 add seat hold table`.

## Testing

Each service runs its own tests with `mvn verify`. CI runs them on every push and pull request (see `.github/workflows/ci.yml`).
