# ADR 0005: The AI agent uses Groq through Spring AI and Telegram as a channel

Status: Accepted — 26 November 2026 (team agreement on Groq).

## Context

The AI Agent Service (ADR 0003) needs an LLM that supports tool calling and a way for customers to talk to it.

## Decision

- The agent calls Groq through Spring AI's OpenAI-compatible client, with a Groq model that supports tool calling. The API key and the Telegram bot token are read from environment variables and are never committed.
- Telegram is a channel into the agent, not a Notification Service. Outbound notifications remain deferred.
- A Telegram chat is not a Keycloak user. Before the agent may call customer-scoped tools (for example, a customer's own bookings) over Telegram, a way to link a Telegram chat to a customer account must be designed. Until then, the Telegram channel may use only public, read-only tools.
- Groq's embeddings support is not confirmed. The embedding source for RAG is decided in T-065.

## Consequences

- Prompts and tool results are sent to an external provider (Groq), so no personal or payment data may be placed in prompts beyond what the customer's request needs.
- The service depends on an external API with rate limits and possible cost.
- ADR 0003 still applies: the agent proposes, the backend decides, and it cannot pay or confirm payment.
