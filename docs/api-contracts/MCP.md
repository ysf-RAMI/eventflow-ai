# MCP tool baseline (later AI phase)

MCP is a tool interface for the future AI Agent Service. It is not a replacement for REST and does not duplicate business logic. Each tool calls the owning service's application/service layer or its secured internal API.

## Catalog MCP tools — read only

- `searchEvents(query?, dateFrom?, dateTo?)`
- `getEventDetails(eventId)`

Implemented in `catalog-service` (T-013) as `EventMcpTools`. The Catalog service hosts an MCP server over Streamable HTTP at `http://localhost:8081/mcp` (Spring AI MCP server starter, protocol `STREAMABLE`). Both tools are marked read-only and call `EventService`, the same service layer as the REST API, so they follow the same rules: only `PUBLISHED` events are returned, and a draft or unknown event is reported as an error.

- `searchEvents`: all parameters are optional. `query` is a case-insensitive title match. `dateFrom` and `dateTo` are ISO date-time strings such as `2026-12-01T00:00:00`, inclusive. Returns a list of events (`id`, `createdBy`, `title`, `description`, `date`, `status`) ordered by date, soonest first. It is not paginated.
- `getEventDetails`: `eventId` is a UUID string. Returns one event.
- Invalid input (a bad UUID, a bad date, or `dateFrom` after `dateTo`) and an unknown or draft event are reported as errors, and the server keeps working.

The endpoint has no authentication until T-050. It exposes the same public data as `GET /api/events`, so do not route `/mcp` publicly before authentication exists. No write tools exist, and `tools/list` returns exactly these two tools. Verified manually with a JSON-RPC script (initialize, tools/list, tools/call); there is no automated test.

## Inventory MCP tools — read only

- `getAvailableSeats(eventId)`
- `checkSeatAvailability(eventId, seatIds)`
- `getSeatDetails(seatId)`

## Booking MCP tools — authenticated customer context

- `getMyBookings(customerId)` — returns only bookings belonging to the specified customer.
- `getBookingStatus(bookingId)` — reads one booking and validates that it exists.

The local Booking implementation currently exposes these tools through Spring AI at
`POST /mcp`. The `createBooking`, `payBooking`, and confirmation operations remain REST-only.

## Prohibited tools

- No `payBooking`, `confirmPayment`, `simulatePayment`, or `refundBooking` tool.
- No direct Inventory hold tool for the agent; hold creation must go through Booking.
- No admin event mutation tools exposed to a customer agent.
- No cancellation of confirmed bookings until cancellation rules are defined.

The agent must run under the authenticated user's identity. Tool parameters and model output are untrusted; authorization and business validation are performed by the backend. MCP server handlers are components/tools, not ordinary Spring MVC `@RestController` endpoints.
