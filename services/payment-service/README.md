# payment-service

A deliberately small payment simulator for the MVP. It does not connect to Stripe, PayPal, a bank, or any real payment provider. It does not move money and does not implement refunds.

- Port: `8084`
- Database: `payment_db` (host port `5435`)
- Interface: internal REST only; no MCP payment tool.

## Payment model

`Payment`: `id`, `bookingId`, `amountCents`, `status`, `createdAt`.

`PaymentStatus`: `PENDING`, `SUCCEEDED`, `FAILED`.

`bookingId` is unique. Repeating the simulation request for the same booking returns the existing result instead of creating another record. The simulation result is controlled by server-side configuration/test profile; customers cannot submit a trusted success/failure result.

## REST baseline

- `POST /internal/payments/simulate`
- `GET /internal/payments/by-booking/{bookingId}`

Only Booking calls these internal endpoints. The customer explicitly confirms the payment step through Booking; Booking then invokes the simulator.

## Java structure

```text
 domain/Payment.java
 domain/PaymentStatus.java
 repository/PaymentRepository.java
 service/PaymentService.java
 service/PaymentSimulator.java
 controller/rest/PaymentRestController.java
 dto/SimulatePaymentRequest.java   (record)
 dto/PaymentResponse.java          (record)
```

## Database

Initial schema baseline: `src/main/resources/db/migration/V1__create_payment_schema.sql`.

## Status

Spring Boot application, Payment entity, PaymentStatus enum, repository, Flyway migration, and PostgreSQL configuration are present. The deterministic simulator, application service, DTO records and internal REST controller are implemented. Service authentication remains pending with the shared identity/infrastructure work; do not expose these endpoints to untrusted clients.

## Running locally

Requires Java 21, Maven and the payment-db container. From the repository root:

```bash
docker compose up -d payment-db
cd services/payment-service
mvn spring-boot:run
```

GET http://localhost:8084/actuator/health reports startup health.
The default simulator result is SUCCEEDED. To simulate failures, restart with
`PAYMENT_SIMULATION_RESULT=FAILED mvn spring-boot:run` in Git Bash.
Only SUCCEEDED and FAILED are accepted; invalid configuration prevents startup.
Existing booking results never change when configuration changes.

## Request and response

POST /internal/payments/simulate accepts:

```json
{"bookingId":"3e46589a-26b9-4b7a-a6b9-569295f6b48b","amountCents":15000}
```

Returns HTTP 200 with id, bookingId, amountCents, status and createdAt.
Both fields are required; amountCents must be a nonnegative integer (zero is
allowed by the existing schema). IDs must be valid UUIDs. Invalid input returns
400. Success/failure is never taken from the request body.

Repeating the same booking and amount returns the saved result, including FAILED.
Reusing the booking with a different amount returns 409. Concurrent inserts use
the unique booking_id constraint; after a conflict, the service rolls back before
reading the winning result in a new transaction. No migration change is required.
GET /internal/payments/by-booking/{bookingId} returns 200 or 404 when absent.

## Verification

Automated tests are deferred by the repository's current task scope. Compilation
is separate from runtime/integration verification. Booking integration and service
authentication remain pending.
