# Prompt for the coding agent

Use this repository as the source of truth for the EventFlow AI MVP. First inspect the entire project, then reconcile and update all source code, SQL migrations, Docker Compose, README files, SRS, ADRs, REST/MCP contracts, UML `.mmd` diagrams, and Java class-design documentation so they agree with the approved decisions in this repository.

Hard constraints:
- No RabbitMQ/Kafka, Outbox/Inbox, AsyncAPI, or asynchronous messaging.
- Inter-service communication is synchronous REST.
- Services: Catalog, Inventory, Booking, Payment Simulator. Notification and AI Agent are deferred.
- Keycloak owns users and CUSTOMER/ADMIN roles; do not create Customer/Admin/Role JPA entities.
- Catalog Event fields only: id, createdBy, title, description, date, status. Do not restore venue fields, startsAt/endsAt, cancellationPolicy, or event audit timestamps.
- Inventory owns Seat and SeatHold, including authoritative hold expiry.
- Booking owns Booking and BookingItem; Booking orchestrates Inventory and Payment REST calls.
- Payment is simulation only; no real provider, refund, or real money movement.
- DTOs must be Java records; JPA entities must be regular classes.
- Keep REST controllers separate from MCP tool components. GraphQL is future scope.
- No cross-service JPA relationships or database foreign keys; use UUID references.
- Notification, AI/RAG, and GraphQL remain deferred.

Current task limits:
- Do not create, modify, or run tests now. Preserve existing test files unchanged.
- Do not claim compilation, runtime, migration, or test success unless actually executed and observed.
- Keep the implementation simple; avoid adding abstractions or dependencies without a documented need.

Workflow:
1. Inspect the repository and identify every contradiction or stale artifact.
2. Update implementation, SQL, configs, and all docs/diagrams together.
3. Keep service boundaries, table ownership, class names, DTO records, routes, and status transitions consistent.
4. Do not silently change approved requirements. If a real conflict cannot be resolved from the docs, report it before making a product decision.
5. Review `git diff`, remove generated/temp files, and ensure secrets are not committed.
6. Create a branch named `chore/eventflow-mvp-alignment`, commit with a clear Conventional Commit message, and push that branch to the configured remote. Do not force-push, rewrite history, or push directly to `main`.
7. In the final report, list changed files, implementation completed, items still pending, and commands/checks not run.

The goal is a coherent corrected project baseline, not a claim that the whole platform is finished.
