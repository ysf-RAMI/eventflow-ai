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
 feign/InventoryServiceRestClient.java
 feign/PaymentServiceRestClient.java
 controllerRest/BookingController.java
 dto/CreateBookingRequest.java
 dto/BookingItemRequest.java
```

The DTO classes are Java records. The JPA entities are regular classes.

## API contracts

The complete provider requirements and response expectations for Inventory and Payment
are defined in [API_CONTRACTS.md](API_CONTRACTS.md).

## Booking REST API

Base URL in the local implementation: `http://localhost:8081`

| Method | Route | Success response | Purpose |
|---|---|---|---|
| POST | `/api/bookings/create` | `201 CREATED` | Create and process a booking |
| GET | `/api/bookings` | `200 OK` | List all bookings |
| GET | `/api/bookings/{bookingId}` | `200 OK` or `404 NOT FOUND` | Get one booking |
| GET | `/api/bookings/customer/{customerId}` | `200 OK` | List bookings by customer |
| GET | `/api/bookings/event/{eventId}` | `200 OK` | List bookings by event |
| DELETE | `/api/bookings/{bookingId}` | `204 NO CONTENT` or `404 NOT FOUND` | Cancel a booking |

### Create booking

```json
{
  "customerId": "11111111-1111-1111-1111-111111111111",
  "eventId": "22222222-2222-2222-2222-222222222222",
  "items": [
    {
      "seatId": "33333333-3333-3333-3333-333333333333",
      "priceCents": 2500
    }
  ]
}
```

The Booking service creates `BookingItem` rows after the Booking ID is generated. The frontend
must not send `bookingId`, `inventoryHoldId`, `status`, `createdAt`, or `holdExpiresAt`.

Swagger UI: `http://localhost:8081/swagger-ui.html`

OpenAPI JSON: `http://localhost:8081/v3/api-docs`

## MCP server

The Booking MCP server is available at `http://localhost:8081/mcp`.

The following read-only tools are exposed with `@McpTool` and `@McpToolParam`:

| MCP tool | Parameters | Purpose |
|---|---|---|
| `getMyBookings` | `customerId` | List bookings for one customer |
| `getBookingStatus` | `bookingId` | Get one booking status |

The MCP tools reuse `BookingService`. They do not expose payment, hold confirmation,
hold release, or booking cancellation operations. Booking MCP therefore cannot simulate
payment or confirm a booking directly.

## Inventory REST contract

Inventory base URL: `http://localhost:8082`

| Method | Route | Purpose |
|---|---|---|
| POST | `/internal/holds/status` | Return status for selected seats |
| POST | `/internal/holds` | Atomically hold selected seats |
| POST | `/internal/holds/{bookingId}/confirm` | Confirm an active hold |
| DELETE | `/internal/holds/{bookingId}` | Release an active hold |

Status request:

```json
{
  "eventId": "22222222-2222-2222-2222-222222222222",
  "seatIds": [
    "33333333-3333-3333-3333-333333333333"
  ]
}
```

Status response:

```json
["AVAILABLE"]
```

Hold request:

```json
{
  "bookingId": "44444444-4444-4444-4444-444444444444",
  "eventId": "22222222-2222-2222-2222-222222222222",
  "seatIds": [
    "33333333-3333-3333-3333-333333333333"
  ],
  "expiresAt": "2026-11-23T15:33:00Z"
}
```

Hold response:

```json
{
  "holdId": "55555555-5555-5555-5555-555555555555",
  "expiresAt": "2026-11-23T15:33:00Z"
}
```

## Payment REST contract

Payment base URL: `http://localhost:8084`

| Method | Route | Purpose |
|---|---|---|
| POST | `/internal/payments/simulate` | Simulate a payment |
| GET | `/internal/payments/by-booking/{bookingId}` | Get a payment by booking |

Simulation request:

```json
{
  "bookingId": "44444444-4444-4444-4444-444444444444",
  "amountCents": 2500
}
```

Simulation response:

```json
{
  "id": "66666666-6666-6666-6666-666666666666",
  "bookingId": "44444444-4444-4444-4444-444444444444",
  "amountCents": 2500,
  "status": "SUCCEEDED",
  "createdAt": "2026-11-23T15:18:57Z"
}
```

Payment statuses: `PENDING`, `SUCCEEDED`, `FAILED`.

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

Calls use bounded timeouts. The Saga is a sequence of local transactions and REST calls, not a distributed database transaction.

## Database

The local implementation uses H2. Migration files are in `src/main/resources/db/migration/`.

## Status

The Spring Boot Booking application includes the REST controller, service orchestration,
JPA entities, repositories, OpenFeign clients, and Swagger/OpenAPI configuration.
Keycloak enforcement and production PostgreSQL configuration remain for later implementation.
