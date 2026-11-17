# MCP tool baseline (later AI phase)

MCP is a tool interface for the future AI Agent Service. It is not a replacement for REST and does not duplicate business logic. Each tool calls the owning service's application/service layer or its secured internal API.

## Catalog MCP tools — read only

- `searchEvents(query, dateFrom?, dateTo?)`
- `getEventDetails(eventId)`

## Inventory MCP tools — read only

- `getAvailableSeats(eventId)`
- `checkSeatAvailability(eventId, seatIds)`
- `getSeatDetails(seatId)`

## Booking MCP tools — authenticated customer context

- `createBooking(eventId, seatIds)` — creates a Booking and requests an Inventory hold; it does not pay.
- `getMyBookings()` — returns only bookings belonging to the authenticated customer.
- `getBookingStatus(bookingId)` — ownership check is mandatory.

## Prohibited tools

- No `payBooking`, `confirmPayment`, `simulatePayment`, or `refundBooking` tool.
- No direct Inventory hold tool for the agent; hold creation must go through Booking.
- No admin event mutation tools exposed to a customer agent.
- No cancellation of confirmed bookings until cancellation rules are defined.

The agent must run under the authenticated user's identity. Tool parameters and model output are untrusted; authorization and business validation are performed by the backend. MCP server handlers are components/tools, not ordinary Spring MVC `@RestController` endpoints.
