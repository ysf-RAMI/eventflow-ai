# ADR 0001: RabbitMQ in the MVP, Kafka later if needed

Status: Proposed (T-010)

## Context
Services communicate through events for booking, payment, and notification flows. The team needs retries, dead-letter queues, and simple setup for a reproducible demo.

## Decision
Use RabbitMQ for the MVP. It supports task and event workflows, retries, and dead-letter queues with little infrastructure.

## Consequences
- Simpler local and cloud setup, and a smaller stack to test.
- No event replay. If replay is needed later, revisit this decision (Kafka is the alternative).
