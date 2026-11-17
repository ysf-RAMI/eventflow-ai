# ADR 0003: The AI agent proposes; the backend decides

Status: Accepted for the later AI phase — 17 November 2026.

## Context

An LLM can misunderstand a request or be manipulated. Booking actions and payment confirmation must be deterministic and authorized.

## Decision

The future agent uses allowlisted MCP tools under the authenticated customer's identity. It may search events, check availability, create a booking hold through Booking, and read the customer's booking status. It cannot simulate or confirm payment. The customer explicitly confirms the payment step through the Booking REST API. The backend performs authorization and state validation.

## Consequences

- The AI agent cannot pay, refund, or mutate event administration data.
- Inventory hold creation is not exposed directly to the agent; it goes through Booking.
- Tool inputs and model output are untrusted and must be validated by backend code.
