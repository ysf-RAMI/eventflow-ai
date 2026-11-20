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

The frontend sends booking request data, not `Booking` or `BookingItem` entities:

```json
{
  "customerId": "customer-uuid",
  "eventId": "event-uuid",
  "items": [
    {
      "seatId": "seat-uuid",
      "priceCents": 2500
    }
  ]
}
```

`bookingId`, `inventoryHoldId`, `status`, `createdAt`, and `holdExpiresAt` are generated or
set by Booking. `BookingItem` rows are created inside the service after the new Booking receives
its generated ID.

The current local development workflow is:

1. Validate the request and selected seats.
2. Ask Inventory for the status of every selected seat.
3. Request an atomic Inventory hold with a 15-minute expiry.
4. Save the Booking as `HELD` and save its BookingItems.
5. Call the simulated Payment client.
6. On payment success, confirm the Inventory hold and mark Booking `CONFIRMED`.
7. On payment failure or an error, release the hold and mark the Booking `FAILED`.
8. On unpaid cancellation, release the active hold and mark Booking `CANCELLED`.

The production contract keeps the same ownership boundary, but uses the Inventory REST service
instead of the local stub. Inventory remains authoritative for seat status and hold expiry.

Calls use bounded timeouts. The Saga is a sequence of local transactions and REST calls, not a distributed database transaction.

## Database

Initial schema baseline: `src/main/resources/db/migration/V1__create_booking_schema.sql`.

## Status

Spring Boot application, Booking and BookingItem entities, BookingStatus enum, repositories, Flyway migration, and PostgreSQL configuration are present. REST clients, saga application service, controllers, Keycloak enforcement, DTO records, and MCP tools remain planned.
