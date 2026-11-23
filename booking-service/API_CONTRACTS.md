# Booking Service REST Contracts

This document defines the REST contracts consumed by the Booking service from the
Inventory and Payment services through OpenFeign.

## Inventory Service

Base URL: `http://localhost:8082`

OpenFeign client: `InventoryServiceRestClient`

### Check seat statuses

```http
POST /internal/holds/status
Content-Type: application/json
```

Request:

```json
{
  "eventId": "22222222-2222-2222-2222-222222222222",
  "seatIds": [
    "33333333-3333-3333-3333-333333333333"
  ]
}
```

Response:

```json
[
  "AVAILABLE"
]
```

Possible seat status values:

- `AVAILABLE`
- `HELD`
- `SOLD`

The returned list must contain one status for each requested seat ID.

### Create a hold

```http
POST /internal/holds
Content-Type: application/json
```

Request:

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

Response:

```json
{
  "holdId": "55555555-5555-5555-5555-555555555555",
  "expiresAt": "2026-11-23T15:33:00Z"
}
```

`holdId` is the Inventory hold identifier. `expiresAt` is the authoritative hold
expiry returned by Inventory.

### Confirm a hold

```http
POST /internal/holds/{bookingId}/confirm
```

Path parameter:

- `bookingId`: UUID of the Booking record.

Success: HTTP `200` or `204`.

Inventory must change confirmed held seats from `HELD` to `SOLD`.

### Release a hold

```http
DELETE /internal/holds/{bookingId}
```

Path parameter:

- `bookingId`: UUID of the Booking record.

Success: HTTP `200` or `204`.

Inventory must release active held seats and change them from `HELD` to `AVAILABLE`.

## Payment Service

Base URL: `http://localhost:8084`

OpenFeign client: `PaymentServiceRestClient`

### Simulate payment

```http
POST /internal/payments/simulate
Content-Type: application/json
```

Request:

```json
{
  "bookingId": "44444444-4444-4444-4444-444444444444",
  "amountCents": 2500
}
```

Response:

```json
{
  "id": "66666666-6666-6666-6666-666666666666",
  "bookingId": "44444444-4444-4444-4444-444444444444",
  "amountCents": 2500,
  "status": "SUCCEEDED",
  "createdAt": "2026-11-23T15:18:57Z"
}
```

Possible payment status values:

- `PENDING`
- `SUCCEEDED`
- `FAILED`

Payment is a simulation only. It must not call a real payment provider.

### Get payment by booking

```http
GET /internal/payments/by-booking/{bookingId}
```

Path parameter:

- `bookingId`: UUID of the Booking record.

Success response:

```json
{
  "id": "66666666-6666-6666-6666-666666666666",
  "bookingId": "44444444-4444-4444-4444-444444444444",
  "amountCents": 2500,
  "status": "SUCCEEDED",
  "createdAt": "2026-11-23T15:18:57Z"
}
```

When no payment exists for the booking, the Payment service should return HTTP `404`.

## Booking Service usage

The Booking workflow uses these contracts in this order:

1. Check every selected seat with Inventory.
2. Create the Booking in `PENDING` state.
3. Create an Inventory hold.
4. Save BookingItems and transition the Booking to `HELD`.
5. Simulate payment through Payment.
6. If payment succeeds, confirm the Inventory hold and set the Booking to `CONFIRMED`.
7. If payment fails or a runtime error occurs, release the hold and set the Booking to
   `FAILED` or keep the cancellation behavior for a cancelled booking.

Inventory and Payment return data only. Booking remains responsible for its local
Booking and BookingItem persistence.
