# Team roles

The team has three or four members. Each role owns one part of the system. With three members, Role 4 is merged into whichever role has the lightest load (usually Role 2). Each role owns the code, tests, and design documents for its services.

Roles are owners, not silos. Every member reviews at least one pull request per week outside their own area.

## Role 1: Booking and concurrency
- Owns: `services/inventory-service`, `services/booking-service`
- Responsible for: seat holds, atomic reservation, booking state machine, Saga orchestration, concurrency tests
- Docs: `docs/adr/0002`, the reservation state machine, OpenAPI for inventory and booking
- Key success measure: 100 concurrent requests for one seat produce exactly one active hold

## Role 2: Payment and messaging
- Owns: `services/payment-service`, RabbitMQ configuration, outbox and inbox code
- Responsible for: payment simulator (success, decline, timeout, delayed confirmation, duplicate callbacks), idempotency keys, transactional outbox, dead-letter queues, circuit breakers
- Docs: AsyncAPI event contracts, payment Merise model, fault experiment reports
- Key success measure: no duplicate charge under repeated requests, and no lost event after a broker outage

## Role 3: AI agent and RAG
- Owns: `services/ai-agent-service`
- Responsible for: Spring AI setup, policy document ingestion into pgvector, RAG with citations, tool definitions, confirmation gate, AI evaluation dataset
- Docs: tool contract (names, parameters, authorization rules), evaluation dataset and results
- Key success measure: the agent cannot cancel, pay, or refund without an explicit confirmation step, and the evaluation set passes at the agreed threshold

## Role 4: Frontend, notifications, and DevOps
- Owns: `frontend/angular-app`, `services/notification-service`, `services/api-gateway`, `.github/workflows`, `docker-compose.yml`, `infrastructure/`
- Responsible for: Angular seat map and booking flow, Telegram bot, Docker images, CI/CD, security baseline (JWT in gateway, OWASP ZAP), AWS deployment (EC2 with K3s), Prometheus and Grafana dashboards
- Key success measure: a clean clone runs the full stack with one command, and CI is green on `develop`

## Shared responsibilities
- **Lead:** whoever the team picks. Merges `develop` to `main`, keeps the board current, and runs the weekly demo.
- **Reviewers:** every pull request needs one approval from someone outside the author's role.
- **Documentation:** the owner of each service keeps its README and contract current.
