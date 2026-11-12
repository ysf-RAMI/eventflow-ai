# inventory-service

Owns seats, seat holds, and availability for every event. Owner: Role 1 (see docs/project/ROLES.md).

## Run locally

From the repo root, start the database first:

```
docker compose up -d inventory-db
```

Then from this folder:

```
mvn spring-boot:run
```

Health check: http://localhost:8082/actuator/health

## Test

```
mvn verify
```

## Current state (first commit)

- `Seat` entity with status rules (`hold`, `release`, `sell`) and unit tests
- Flyway V1 creates `event_seat`
- Health endpoint

## Planned

- T-021: `seat_hold` table with one ACTIVE hold per seat
- T-022: atomic hold endpoint with concurrency test
- T-023: expiry job
- T-012: OpenAPI contract in docs/api-contracts
