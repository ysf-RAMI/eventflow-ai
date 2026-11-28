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

## Payment implementation follow-up (T-041 / T-042)

Payment now includes its DTO records, deterministic simulator, application service
and internal REST controller. Duplicate booking requests return the stored result;
conflicting amounts return 409. Concurrent insertion conflicts are recovered after
rollback. No schema changes were needed. Booking integration and internal endpoint
authentication remain pending. This update does not establish end-to-end readiness.


## Catalog implementation follow-up (T-012)

Catalog now includes Event update/delete rules, DTO records, a title and date-range search query, `EventService`, `EventRestController` and error handling. The six REST routes were verified manually with curl against a local PostgreSQL; no automated tests were added. The detailed contract is `docs/api-contracts/CATALOG_REST.md`. `ADMIN` enforcement and the real `createdBy` source (Keycloak JWT) are pending with T-050; until then `X-User-Id` is a placeholder header. MCP tools (T-013) were pending at that point (see the T-013 section below). No schema changes were needed. This update does not establish end-to-end readiness.

## Catalog–Inventory seat provisioning (design change)

The team decided that Catalog generates an event's seats and prices when the event is published and sends them to Inventory (`POST /internal/events/{eventId}/seats`), and that Inventory checks the event is published before opening a hold. Admin seat management in Inventory is removed. See `docs/adr/0004-catalog-provisions-seats-on-publish.md`. SRS FR-011, `REST.md`, `CLASS_DESIGN.md` and `BACKLOG.md` (T-014, T-025, T-026) are updated.The Catalog side is implemented in T-014 (below); Inventory's side (T-025, T-026) is pending. This update does not establish end-to-end readiness.

## Catalog implementation follow-up (T-014)

Catalog now generates an event's seats and prices on publish and sends them to Inventory through an OpenFeign client (`POST /internal/events/{eventId}/seats`, 2 second connect timeout, 5 second read timeout, no retries). If provisioning fails for any reason, `publish` returns `503` and the event stays `DRAFT`. This was verified manually against a fake Inventory server (success, connection failure, `500` and timeout); no automated tests were added and it has not been tested against the real Inventory service. Inventory's provisioning endpoint (T-025) is pending and owned by Inventory. This update does not establish end-to-end readiness.

## Catalog implementation follow-up (T-013)

Catalog now hosts read-only MCP tools (`searchEvents`, `getEventDetails`) at `/mcp`, using the Spring AI MCP server starter over Streamable HTTP. The tools call `EventService` and return published events only. To use the starter, Catalog was upgraded to Spring Boot 4.1.2 and Spring Cloud 2025.1.5, the same versions Booking uses; the existing REST and seat-provisioning checks were rerun and passed after the upgrade. A JSON-RPC script verified the tool list, searches, details and error cases; no automated tests were added. The `/mcp` endpoint has no authentication until T-050. The Inventory and Booking MCP tools are not implemented. This update does not establish end-to-end readiness.

### `inventory-service`
- **T-020 (Done):** `Seat` entity, repository, and Flyway `V1` schema migration.
- **T-021 (Done):** `SeatHold` entity, `HoldStatus` enum, repository, and Flyway `V2` migration enforcing one active hold per seat.
- **T-025 (Done):** `POST /internal/events/{eventId}/seats` idempotent provisioning endpoint (`201` on create, `200` on idempotent retry, `400` on validation errors). Verified via `mvn test`, direct `curl` + PostgreSQL checks, and end-to-end event publishing from `catalog-service`.
- **T-022 / T-023 / T-024 / T-026 (Pending):** Seat hold lifecycle endpoints, hold expiry scheduler, read queries/MCP tools, and Catalog published-event check are not yet implemented.

## AI Agent skeleton (T-060)

`ai-agent-service` now exists as a Spring Boot 4.1 skeleton on port 8085 with its own `ai_db` (PostgreSQL 16 with pgvector, port 5436). Flyway applies one migration that installs the `vector` extension. Verified manually: the application starts, health is UP, and the extension is installed. There are no endpoints, LLM calls, tools or tests yet. Groq is the agreed LLM provider (ADR 0005); linking a Telegram chat to a customer is not designed yet. This update does not establish end-to-end readiness.

## AI Agent chat endpoint (T-061)

`ai-agent-service` now answers `POST /api/agent/chat` through a Groq model using Spring AI 2.0.2. The default model is `openai/gpt-oss-120b` (Groq retired `llama-3.3-70b-versatile` on 16 August 2026). The API key comes from the `GROQ_API_KEY` environment variable and is not committed. A blank message or one over 2000 characters returns `400`, and a model failure returns `503`. Four automated tests pass: a real Groq chat request, the two validation cases, and a simulated model failure. The real-request test needs a Groq API key. The endpoint has no authentication, conversation history or tools yet. This update does not establish end-to-end readiness.
