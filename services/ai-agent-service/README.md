# ai-agent-service — later phase

Planned Spring AI agent with RAG and allowlisted MCP tools. It acts only under the authenticated customer's identity.

Planned database: `ai_db` with `policy_document` and `document_chunk` (pgvector). This database is not included in the current Docker Compose core stack.

The agent may search events, check availability, create a booking hold through Booking MCP tools, and read the current customer's bookings. It must not confirm payment, simulate payment, refund, or use admin mutation tools. MCP implementation is deferred until the core REST services are stable.
