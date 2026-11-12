# Software Requirements Specification: EventFlow AI

Version 0.1, draft for team review (task T-003).

## 1. Purpose and scope

EventFlow AI is a distributed platform for booking seats at events such as concerts and conferences. Customers search events, hold seats, pay, and manage bookings. An AI agent answers questions from policy documents and can take booking actions through controlled tools.

The MVP covers booking, concurrency, payment, Saga compensation, the AI agent, and notifications. Redis caching beyond rate limiting, Kubernetes on managed EKS, and real payment providers are out of scope for the MVP.

## 2. Actors

- **Customer:** searches events, holds seats, pays, cancels, and asks questions through the web app, Telegram, or the AI agent.
- **Organizer (admin):** creates events and seat maps and publishes events.
- **AI agent:** acts on behalf of an authenticated customer, through allowlisted tools only.
- **Payment simulator:** stands in for an external payment provider.
- **Telegram Bot API:** receives notifications.

## 3. Functional requirements

| ID | Requirement |
|---|---|
| FR-001 | The system lists events and filters them by date and price. |
| FR-002 | The system shows the real-time availability of each seat for an event. |
| FR-003 | A customer can hold one or more available seats for a limited time (default 10 minutes). |
| FR-004 | A customer can create a booking from held seats. |
| FR-005 | A customer can pay for a booking through the payment service. |
| FR-006 | A booking is confirmed only when payment succeeded and the hold is still valid. |
| FR-007 | Holds that are not paid before they expire are released. |
| FR-008 | A customer can cancel a confirmed booking, and the system refunds it according to the cancellation policy. |
| FR-009 | Customers are notified of booking confirmed, payment failed, and hold expired events. |
| FR-010 | A customer can view the status of each booking. |
| FR-011 | An organizer can create events, define seat maps, and publish events. |
| FR-012 | The AI agent answers policy questions from indexed documents, with citations. |
| FR-013 | The AI agent searches events and checks seat availability through tools. |
| FR-014 | The AI agent can hold seats and request cancellation only through controlled tools. |
| FR-015 | Payment and cancellation actions require explicit confirmation from the customer. |
| FR-016 | Every agent action runs under the authenticated customer's identity and is authorized by the backend. |

## 4. Non-functional requirements

| ID | Requirement |
|---|---|
| NFR-001 | Correctness under concurrency: with 100 concurrent requests for one seat, at most one active hold exists. |
| NFR-002 | Idempotency: repeating a payment request never creates a second charge. |
| NFR-003 | Consistency: no booking is confirmed without a successful payment. |
| NFR-004 | Messaging: events are delivered at least once, and consumers process them idempotently. |
| NFR-005 | Data ownership: each service owns its database, and there are no cross-service foreign keys. |
| NFR-006 | Resilience: when the payment service is down, state stays consistent and compensations run. |
| NFR-007 | Observability: each service exposes health, metrics, logs, and traces. |
| NFR-008 | Reproducibility: the full stack runs with Docker Compose, and the cloud deployment runs on K3s on AWS. |

## 5. Business rules

- The AI never decides whether a booking or payment happens. The backend does, using deterministic rules.
- Refund eligibility follows the event's cancellation policy (for example, refunds allowed up to 48 hours before the event).

## 6. Quality gates (Phase 4)

- Zero double bookings in the concurrency test.
- Zero duplicate simulated charges in the duplicate-callback test.
- All critical workflows pass end to end.
- Recovery is measurable after injected failures (payment down, broker down).
- Unit test coverage of at least 80% on critical domain logic. Coverage does not replace correctness tests.

## 7. Open questions for the team

- Does a seat belong to one event, or can seat maps be reused as templates?
- Is refund a separate entity or a negative payment?
- Will authentication use a built-in JWT issuer or an external provider such as Keycloak?
