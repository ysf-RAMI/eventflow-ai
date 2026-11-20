# REST interface baseline

These routes are the agreed MVP contract baseline. Exact request/response records and validation annotations will be added when each service is implemented. Internal routes must not be exposed directly to untrusted clients; use service authentication/network policy in addition to user JWT authorization.

## Catalog Service — port 8081

| Method | Route | Access | Purpose |
|---|---|---|---|
| POST | `/api/events` | ADMIN | Create draft event |
| GET | `/api/events` | Authenticated / public read policy to decide in implementation | List/search events |
| GET | `/api/events/{eventId}` | Authenticated / public read policy to decide in implementation | Event details |
| PUT | `/api/events/{eventId}` | ADMIN | Update event |
| POST | `/api/events/{eventId}/publish` | ADMIN | Publish event |
| DELETE | `/api/events/{eventId}` | ADMIN | Delete eligible draft event |

Event fields: `id`, `createdBy`, `title`, `description`, `date`, `status`. `date` is represented as `LocalDateTime` and stored as `event_date`.

## Inventory Service — port 8082

| Method | Route | Access | Purpose |
|---|---|---|---|
| POST | `/api/events/{eventId}/seats` | ADMIN | Create seats |
| GET | `/api/events/{eventId}/seats` | Authenticated | List seats |
| GET | `/api/events/{eventId}/availability` | Authenticated | Availability summary/details |
| PATCH | `/api/seats/{seatId}` | ADMIN | Update eligible seat fields |
| DELETE | `/api/seats/{seatId}` | ADMIN | Delete eligible unsold/unheld seat |
| POST | `/internal/holds/status` | Booking service only | Return the current status for requested seat IDs |
| POST | `/internal/holds` | Booking service only | Atomically hold multiple seats and return prices and expiry |
| POST | `/internal/holds/{bookingId}/confirm` | Booking service only | Confirm active hold; seats become SOLD |
| DELETE | `/internal/holds/{bookingId}` | Booking service only | Release active holds for booking |

`POST /internal/holds/status` request concept: `eventId`, `seatIds`. The response contains one
status for each requested seat. Booking proceeds only when every status is `AVAILABLE`.

`POST /internal/holds` request concept: `bookingId`, `eventId`, `seatIds`, and `expiresAt`.
Response concept: `bookingId`, a generated `holdId`, held seat details (`seatId`, `label`,
`priceCents`), and the authoritative `expiresAt`.

Booking stores only the returned `holdId` and price snapshots. Inventory owns the seat rows and
performs `AVAILABLE → HELD → SOLD` or `HELD → AVAILABLE` transitions.

## Booking Service — port 8083

| Method | Route | Access | Purpose |
|---|---|---|---|
| POST | `/api/bookings` | CUSTOMER | Create booking and request Inventory hold |
| GET | `/api/bookings/me` | CUSTOMER | List current customer's bookings |
| GET | `/api/bookings/{bookingId}` | Owner / ADMIN | Get booking details |
| POST | `/api/bookings/{bookingId}/confirm-payment` | Owner / CUSTOMER | Explicitly confirm simulated payment |
| POST | `/api/bookings/{bookingId}/cancel` | Owner / CUSTOMER | Cancel unpaid HELD booking and release hold |

The customer ID is derived from the authenticated Keycloak JWT subject, never accepted from the request body.

## Payment Service — port 8084

| Method | Route | Access | Purpose |
|---|---|---|---|
| POST | `/internal/payments/simulate` | Booking service only | Create or return the single simulated result for a booking |
| GET | `/internal/payments/by-booking/{bookingId}` | Booking service only | Retrieve the simulation result |

The payment simulator does not connect to a real provider and does not move money. One unique payment record is allowed per booking. The simulator's success/failure mode is controlled by server-side configuration/test profile, not by an untrusted customer request.

## Error handling baseline

- `400`: invalid request or invalid state transition.
- `401`: missing/invalid authentication.
- `403`: authenticated user lacks permission or does not own the booking.
- `404`: resource not found.
- `409`: seat/hold conflict or booking state conflict.
- `503`: dependent service unavailable or timed out.

All inter-service calls require bounded connect/read timeouts. Retries must be limited and used only for idempotent operations.

### Payment implementation details (T-041 / T-042)

POST `/internal/payments/simulate` requires `{ "bookingId": "UUID", "amountCents": 15000 }`.
It returns HTTP 200 and `{ id, bookingId, amountCents, status, createdAt }`.
Missing fields, malformed UUIDs or negative amounts return 400. Zero is allowed.
Identical retries return the original stored result. A different amount for an
existing booking returns 409; the existing result is unchanged. Concurrent
requests are deduplicated using the unique booking_id database constraint.
GET returns the same response, or 404 if the booking has no payment.
`PAYMENT_SIMULATION_RESULT` selects SUCCEEDED (default) or FAILED on the server.
These routes are implemented; service authentication is still pending and they
must remain restricted to trusted local development until it is integrated.
