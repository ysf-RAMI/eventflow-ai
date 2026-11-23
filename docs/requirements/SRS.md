# Software Requirements Specification — EventFlow AI

Version 1.0 — approved architecture baseline, 17 November 2026.

## 1. Purpose and scope

EventFlow AI is a distributed platform for discovering events and reserving seats. The MVP includes event CRUD, seat availability, temporary seat holds, booking orchestration, and a simulated payment flow. It uses synchronous REST between services and PostgreSQL as each service's source of truth.

The MVP does not process real payments, implement refunds, send notifications, or use RabbitMQ/Kafka. AI/RAG and notification services are later phases. GraphQL is a future extension.

## 2. Actors

- **Customer:** browses events, checks availability, creates a booking, confirms a simulated payment, and views booking status.
- **Admin:** creates, updates, publishes, and manages events. Publishing an event provisions its seats automatically.
- **AI agent (later phase):** answers policy questions and uses allowlisted MCP tools under the authenticated customer's identity. It cannot confirm payment.
- **Payment simulator:** records a simulated success/failure result; it is not a real payment provider.
- **Keycloak:** authenticates users and supplies `CUSTOMER` / `ADMIN` roles.

## 3. Functional requirements

| ID | Requirement | Service |
|---|---|---|
| FR-001 | The system lists and searches published events and can filter by event date. | Catalog |
| FR-002 | The system shows seat details and current availability for an event. | Inventory |
| FR-003 | A customer can request a temporary hold on one or more available seats; default duration is 10 minutes. | Booking + Inventory |
| FR-004 | Booking creates a `PENDING` booking before requesting the Inventory hold. If the hold succeeds, it stores one booking item per seat and changes the booking to `HELD`. | Booking |
| FR-005 | A customer must explicitly confirm the simulated payment step. | Booking |
| FR-006 | The booking becomes `CONFIRMED` only after Payment returns `SUCCEEDED` and Inventory confirms the still-active hold. | Booking |
| FR-007 | Inventory expires unpaid holds and returns their seats to `AVAILABLE`. | Inventory |
| FR-008 | A customer can cancel a `HELD` booking before payment; Booking asks Inventory to release its active hold. Cancellation of a confirmed booking and refunds are outside the MVP. | Booking + Inventory |
| FR-009 | A customer can view only their own bookings and booking statuses. | Booking |
| FR-010 | An admin can create, read, update, delete eligible draft events, and publish events. | Catalog |
| FR-011 | When an admin publishes an event, Catalog generates its seats and prices and Inventory stores them as `AVAILABLE`. If seat provisioning fails, the event stays `DRAFT`. Admins do not create, update or delete seats directly. | Catalog + Inventory |
| FR-012 | The future AI agent answers policy questions from indexed documents with citations. | AI Agent (later) |
| FR-013 | The future AI agent can search events and check seat availability through read-only MCP tools. | AI Agent (later) |
| FR-014 | The future AI agent may create a booking/temporary hold only through Booking MCP tools; it cannot pay or confirm payment. | AI Agent + Booking (later) |
| FR-015 | Payment simulation requires explicit customer confirmation enforced by the backend. | Booking |
| FR-016 | Every customer operation is authorized using the authenticated identity; admin-only operations require the `ADMIN` role. | Gateway / services |

## 4. Non-functional requirements

| ID | Requirement |
|---|---|
| NFR-001 | With 100 concurrent requests for the same seat, at most one active hold exists. |
| NFR-002 | Repeating a payment simulation for the same booking does not create a second payment record. |
| NFR-003 | A booking is never marked `CONFIRMED` unless payment simulation succeeded and Inventory confirmed the hold. |
| NFR-004 | Each business service owns its PostgreSQL database; there are no cross-service foreign keys. |
| NFR-005 | Inter-service REST calls use bounded timeouts and explicit error handling. |
| NFR-006 | The local database stack is reproducible with Docker Compose. |
| NFR-007 | Critical domain rules have unit tests; seat concurrency and end-to-end booking have integration tests. |
| NFR-008 | Passwords and production credentials are never committed to source control. |

## 5. Data and identity rules

- Keycloak owns user accounts, passwords, and roles. There are no `Customer`, `Admin`, or `Role` JPA entities in business services.
- `customer_id` and `created_by` store the Keycloak JWT subject (`sub`) as UUID values.
- `event_id`, `seat_id`, and `booking_id` in other services are external UUID references, not cross-service SQL foreign keys.
- Event has one `date` field (Java `LocalDateTime`, database `event_date`). Venue fields, start/end range, cancellation policy, and event audit timestamps are not part of the approved MVP Event model.
- `hold_expires_at` in Booking is a snapshot. `SeatHold.expires_at` in Inventory is authoritative.

## 6. Business rules

- The AI agent proposes actions; the backend authorizes and executes them deterministically.
- A seat can have at most one `ACTIVE` hold.
- All seats in one hold request are acquired atomically: if any requested seat is unavailable, none are held.
- The default hold duration is 10 minutes.
- A `HELD` seat can become `SOLD` only after the payment simulation succeeds and Inventory confirms the hold.
- Payment is simulated only. No real funds are collected and no refund workflow is implemented.
- Confirmed-booking cancellation is deferred until a cancellation policy is designed.
- Notifications are deferred and must not be a dependency of booking correctness when later introduced.

## 7. MVP quality gates

- 100 concurrent requests for one seat produce at most one active hold.
- An all-or-nothing multi-seat hold leaves no partial holds when one seat is unavailable.
- Repeated payment simulation for a booking creates one Payment record.
- A booking cannot reach `CONFIRMED` if payment fails or the hold expires before Inventory confirmation.
- Customer A cannot read or modify Customer B's booking.
- Admin-only event management rejects a `CUSTOMER` token.
- Publishing an event whose seat provisioning fails leaves it `DRAFT`; retrying publish does not create duplicate seats.
- The core database stack starts with `docker compose up -d`.

## 8. Deferred scope

- Real payment providers, refunds, and confirmed-booking cancellation policy.
- RabbitMQ, Kafka, Outbox, Inbox, AsyncAPI, and asynchronous event delivery.
- Notification service and Telegram integration.
- AI/RAG implementation and pgvector deployment.
- GraphQL, Redis caching, Kubernetes, and cloud deployment.
