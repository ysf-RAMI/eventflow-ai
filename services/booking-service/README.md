# booking-service

Owns customer bookings, booking items, booking state transitions, and the REST Saga orchestration. It does not own Customer, Event, Seat, SeatHold, or Payment entities.

- Port: `8083`
- Database: `booking_db` (host port `5434`)
- Dependencies: Inventory REST and Payment REST
- No RabbitMQ, Outbox, Inbox, or asynchronous consumers.

## Domain model

### Booking

`id`, `customerId`, `eventId`, `status`, `totalCents`, `holdExpiresAt`, `createdAt`.

- `customerId` is derived from the authenticated Keycloak JWT subject (`sub`).
- `eventId` is an external Catalog UUID.
- `holdExpiresAt` is a snapshot returned by Inventory. Inventory's `SeatHold.expiresAt` remains authoritative.
- `totalCents` is populated after Inventory successfully holds seats and returns their prices.

### BookingItem

`id`, `bookingId`, `seatId`, `priceCents`.

One row represents one seat in a booking. `bookingId` is a local FK to `booking`; `seatId` is an external Inventory UUID. `priceCents` is the price snapshot at booking time.

### BookingStatus

`PENDING`, `HELD`, `CONFIRMED`, `FAILED`, `EXPIRED`, `CANCELLED`.

## Java structure

```text
 domain/Booking.java
 domain/BookingItem.java
 domain/BookingStatus.java
 repository/BookingRepository.java
 repository/BookingItemRepository.java
 service/BookingService.java
 service/BookingSagaOrchestrator.java
 client/InventoryClient.java
 client/PaymentClient.java
 controller/rest/BookingRestController.java
 controller/mcp/BookingMcpTools.java
 dto/CreateBookingRequest.java       (record)
 dto/BookingResponse.java            (record)
```

MCP tools can create a booking hold and read the authenticated customer's bookings/status. They cannot pay or confirm payment. GraphQL is future scope.

## REST Saga

1. Persist Booking as `PENDING`.
2. Call Inventory to atomically hold all requested seats.
3. On success, persist BookingItems, total and hold expiry; transition to `HELD`.
4. Wait for explicit customer confirmation.
5. Call Payment simulator.
6. On simulated success, ask Inventory to confirm the active hold; only then mark Booking `CONFIRMED`.
7. On failure, expiry, or unpaid cancellation, release any still-active hold and update Booking state.

Calls use bounded timeouts. The Saga is a sequence of local transactions and REST calls, not a distributed database transaction.

## Database

Initial schema baseline: `src/main/resources/db/migration/V1__create_booking_schema.sql`.

## Status

Spring Boot application, Booking and BookingItem entities, BookingStatus enum, repositories, Flyway migration, and PostgreSQL configuration are present. REST clients, saga application service, controllers, Keycloak enforcement, DTO records, and MCP tools remain planned.
