# Project phases

The module runs from early November to 13 December 2026. The phases below follow the module roadmap. Each phase has a goal, a list of deliverables, and a Definition of Done. A phase is not done until its Definition of Done is met.

## Phase 0: Setup (Nov 2 to 6)
Goal: a working repo and a runnable first service.
- Repo, branches, CI, and docker-compose (T-001, T-002)
- Inventory service starter with health endpoint (T-020)
- Definition of Done: `docker compose up -d` and `mvn verify` pass on a clean clone.

## Phase 1: Requirements (Nov 2 to 6)
Goal: agree on what the system must do.
- SRS with numbered functional and non-functional requirements (docs/requirements/SRS.md)
- Use case diagram and actor list (docs/uml)
- Definition of Done: every requirement has an ID, and the team has reviewed and approved the SRS.

## Phase 2: Conception (Nov 2 to 6, continued in parallel with Phase 3)
Goal: design the system before writing feature code.
- Class, sequence, and state diagrams (docs/uml, docs/architecture)
- C4 container diagram
- OpenAPI contracts for inventory and booking, AsyncAPI for events
- Merise data model (MCD, MLD, MPD) per service
- ADRs for each major decision (docs/adr)
- Definition of Done: each service has a reviewed contract and data model, and the three ADRs are accepted.

## Phase 3: Development (Nov 7 to Dec 7)
Built in four increments. Each increment ends with a demo and merged work on `develop`.

- **Increment A, core booking (Nov 7 to 15):** seat holds, booking creation, concurrency tests. Owner: Role 1.
- **Increment B, transactions (Nov 16 to 24):** RabbitMQ, payment simulator, idempotency, outbox, Saga compensation, circuit breakers, notifications. Owners: Roles 2 and 1, with Role 4 on notifications.
- **Increment C, AI agent (Nov 25 to Dec 2):** Spring AI, RAG ingestion, tool calling, confirmation gate, AI evaluation set. Owner: Role 3.
- **Increment D, frontend and DevOps (Dec 3 to 7):** Angular app, Telegram bot, Docker images, full-stack compose, security baseline, AWS deployment. Owner: Role 4.

Definition of Done for each task: code and unit tests merged through a reviewed pull request, CI green, and acceptance criteria in BACKLOG.md met.

## Phase 4: QA and validation (Dec 8 to 13)
- Integration tests with Testcontainers for each service
- End-to-end browser tests (Playwright)
- Concurrency and load tests (k6): 100 concurrent requests for one seat must produce one active hold
- Fault experiments: stop payment service, stop RabbitMQ, duplicate event delivery
- OWASP ZAP scan and Trivy image scan
- AI evaluation: retrieval quality, answer correctness, tool-selection accuracy
- Definition of Done: all quality gates in the "Quality gates" section of the SRS pass.

## Phase 5: Observability and delivery (Dec 8 to 13)
- Actuator, Prometheus, Grafana dashboard, Loki logs, Tempo traces
- Final report, architecture diagrams, and 5-minute demo
- Definition of Done: a grader can reproduce the demo from the README in under 15 minutes.

## Optional extensions (only after the MVP passes)
Redis caching beyond rate limiting, Kubernetes on managed EKS, feature flags, automated rollback. These are not part of the MVP and must not block it.
