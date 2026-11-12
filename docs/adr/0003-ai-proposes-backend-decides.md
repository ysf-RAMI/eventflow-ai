# ADR 0003: The AI agent proposes; the backend decides

Status: Proposed (T-010)

## Context
An LLM can misunderstand a request or be manipulated by prompt injection. Booking and payment must be deterministic and authorized.

## Decision
The agent calls only allowlisted tools. Each tool runs under the authenticated customer's identity. Payment and cancellation require an explicit confirmation step from the customer, enforced by the backend, not by the model.

## Consequences
- The agent cannot pay, cancel, or refund on its own.
- Each tool needs its own authorization tests (T-043, T-044).
