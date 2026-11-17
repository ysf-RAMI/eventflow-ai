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

Spring Boot application, Payment entity, PaymentStatus enum, repository, Flyway migration, and PostgreSQL configuration are present. The deterministic simulator service and internal REST controller remain planned.
