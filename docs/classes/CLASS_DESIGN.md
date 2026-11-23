# Java class design — EventFlow AI MVP

This document is the class-level implementation guide. It describes ownership and responsibilities; it does not claim that every listed class has already been implemented.

## Global conventions

- Java 21 and Spring Boot 3.3.
- JPA entities are regular Java classes.
- Request/response DTOs are Java `record` types.
- Enums are Java enums, not separate database tables.
- Repositories belong in `repository/`, never in `domain/`.
- Business rules belong in `service/` and domain methods, not controllers.
- REST controllers and MCP tool components are separate.
- MCP tools call the same application/service logic as REST; they do not duplicate business rules.
- Cross-service IDs are UUID scalar fields. Do not create cross-service `@ManyToOne` relationships.

## Catalog Service

### Domain
- `Event`: `id`, `createdBy`, `title`, `description`, `date`, `status`.
- `EventStatus`: `DRAFT`, `PUBLISHED`, `CANCELLED`, `COMPLETED`.

### Application and adapters
- `EventRepository`: persistence for Event.
- `EventService`: create, update, publish (provisions seats in Inventory before changing the status), delete eligible draft events, list/search, get details.
- `EventRestController`: admin CRUD and event read endpoints.
- `EventMcpTools`: read-only search/details tools for the future AI agent.
- DTO records: `CreateEventRequest`, `UpdateEventRequest`, `EventResponse`.
- `SeatGenerator`: generates an event's seats. The count is a random whole number from 70 to 100, each price is a random whole number from 100 to 1000 currency units converted to `priceCents`, and the generator is seeded from the event id so a retry produces identical seats.
- `InventoryClient`: OpenFeign client for Inventory `POST /internal/events/{eventId}/seats`; timeouts are set in `application.yml`. `EventService.publish` wraps any `FeignException` in `SeatProvisioningException`.
- `SeatProvisioningException`: handled by `GlobalExceptionHandler` as `503`.
- Provisioning DTO records: `ProvisionSeatsRequest`, `SeatSpec`.
- `EventNotFoundException`, `GlobalExceptionHandler`: error handling (404 / 400).

### Suggested package layout
```text
catalog/
  domain/Event.java
  domain/EventStatus.java
  repository/EventRepository.java
  service/EventService.java
  controller/rest/EventRestController.java
  controller/mcp/EventMcpTools.java
  dto/CreateEventRequest.java
  dto/UpdateEventRequest.java
  dto/EventResponse.java
  exception/EventNotFoundException.java
  exception/GlobalExceptionHandler.java
  service/SeatGenerator.java
  client/InventoryClient.java
  dto/ProvisionSeatsRequest.java
  dto/SeatSpec.java
  exception/SeatProvisioningException.java
```

`Event.date` is one `LocalDateTime` property stored as `event_date`. Do not reintroduce venue fields, start/end fields, cancellation policy, or event audit timestamps in the approved MVP entity.

## Inventory Service

### Domain
- `Seat`: `id`, `eventId`, `label`, `section`, `priceCents`, `status`, `version`.
- `SeatHold`: `id`, `seatId`, `bookingId`, `expiresAt`, `status`, `createdAt`.
- `SeatStatus`: `AVAILABLE`, `HELD`, `SOLD`.
- `HoldStatus`: `ACTIVE`, `CONFIRMED`, `RELEASED`, `EXPIRED`.

### Application and adapters
- `SeatRepository`: persistence and lookup of seats.
- `SeatHoldRepository`: persistence and lookup of holds.
- `SeatService`: provision seats for an event (idempotent: if the event already has seats, nothing changes), list seats and availability queries. Admins no longer update or delete seats.
- `SeatHoldService`: atomically hold all requested seats, confirm active holds, release active holds. Before the hold transaction starts, it asks `CatalogClient` whether the event is published.
- `CatalogClient` (planned): calls Catalog `GET /api/events/{eventId}` with bounded timeouts. A missing event is rejected; an unavailable Catalog returns `503`.
- `HoldExpiryScheduler`: expire due holds and return their seats to `AVAILABLE`.
- `InventoryRestController`: public seat/availability routes and internal hold routes.
- `InventoryMcpTools`: read-only availability and seat detail tools.
- DTO records: `ProvisionSeatsRequest`, `SeatSpec`, `SeatResponse`, `AvailabilityResponse`, `CreateHoldRequest`, `HoldResponse`.

### Suggested package layout
```text
inventory/
  domain/Seat.java
  domain/SeatStatus.java
  domain/SeatHold.java
  domain/HoldStatus.java
  repository/SeatRepository.java
  repository/SeatHoldRepository.java
  service/SeatService.java
  service/SeatHoldService.java
  service/HoldExpiryScheduler.java
  client/CatalogClient.java                 (planned)
  controller/rest/InventoryRestController.java
  controller/mcp/InventoryMcpTools.java
  dto/*.java
```

Inventory is the source of truth for hold expiry. A hold lasts 10 minutes by default. The partial unique index allows at most one `ACTIVE` hold per seat. Multi-seat hold creation is all-or-nothing in one Inventory database transaction.

## Booking Service

### Domain
- `Booking`: `id`, `customerId`, `eventId`, `status`, `totalCents`, `holdExpiresAt`, `createdAt`.
- `BookingItem`: `id`, `bookingId`, `seatId`, `priceCents`.
- `BookingStatus`: `PENDING`, `HELD`, `CONFIRMED`, `FAILED`, `EXPIRED`, `CANCELLED`.

### Application and adapters
- `BookingRepository`: persistence and customer-owned booking queries.
- `BookingItemRepository`: persistence for booking items.
- `BookingService`: local booking use cases and legal status transitions.
- `BookingSagaOrchestrator`: coordinates Inventory and Payment REST calls and compensation.
- `InventoryClient`: typed REST client for Inventory internal endpoints.
- `PaymentClient`: typed REST client for Payment simulator endpoints.
- `BookingRestController`: create booking, read own booking, confirm simulated payment, cancel unpaid booking.
- `BookingMcpTools`: create a hold and read own booking/status; never trigger payment.
- DTO records: `CreateBookingRequest`, `BookingResponse`, `BookingItemResponse`, and client request/response records as needed.

### Suggested package layout
```text
booking/
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
  dto/*.java
```

`customerId` comes from the authenticated Keycloak JWT `sub`, never from the customer request body. `eventId` and `seatId` are external UUIDs. `holdExpiresAt` is a display/read snapshot from Inventory; it is not authoritative.

`BookingItem` represents one seat line. A booking for four seats has one `booking` row and four `booking_item` rows. `booking.booking_id` is a local FK; `booking_item.seat_id` is an external Inventory UUID.

## Payment Service

### Domain
- `Payment`: `id`, `bookingId`, `amountCents`, `status`, `createdAt`.
- `PaymentStatus`: `PENDING`, `SUCCEEDED`, `FAILED`.

### Application and adapters
- `PaymentRepository`: persistence; `bookingId` is unique.
- `PaymentService`: creates or returns the existing simulation for a booking.
- `PaymentSimulator`: server-controlled deterministic simulation result.
- `PaymentRestController`: internal REST-only endpoints.
- DTO records: `SimulatePaymentRequest`, `PaymentResponse`.

### Suggested package layout
```text
payment/
  domain/Payment.java
  domain/PaymentStatus.java
  repository/PaymentRepository.java
  service/PaymentService.java
  service/PaymentSimulator.java
  controller/rest/PaymentRestController.java
  dto/SimulatePaymentRequest.java
  dto/PaymentResponse.java
```

Payment is a simulation only. No real provider, bank integration, card data, refund entity, refund table, or MCP payment tool.

## Identity and roles

Keycloak owns users, passwords, and roles (`CUSTOMER`, `ADMIN`). Do not create `Customer`, `Admin`, or `Role` JPA entities in these business services. Use the JWT subject as a UUID reference.

## Deferred services

- API Gateway: planned edge service for JWT validation/routing.
- AI Agent: later phase; RAG documents/chunks and restricted MCP tools.
- Notification: later phase; no notification implementation or database in the MVP.
- GraphQL: future API adapter, not part of current scope.
