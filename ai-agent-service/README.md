# ai-agent-service

The AI agent for EventFlow AI. It answers customer messages with an LLM (Groq, through Spring AI) and will later call the other services' read-only MCP tools, answer policy questions from indexed documents (RAG), and talk to customers through Telegram. It owns only its own database, `ai_db`, and never holds business source-of-truth data. See ADR 0003 and ADR 0005.

- Port: `8085`
- Database: `ai_db` (host port `5436`), PostgreSQL 16 with the pgvector extension
- Stack: Java 21, Spring Boot 4.1, Spring AI 2.0 (Groq through the OpenAI-compatible client), Flyway

## Local run

From the repository root:

```bash
docker compose up -d ai-db
cd ai-agent-service
cp .env.example .env        # then put your own Groq API key in .env
set -a; source .env; set +a
mvn spring-boot:run
```

Health: http://localhost:8085/actuator/health

## Chat endpoint

`POST /api/agent/chat`

```json
{ "message": "Say hello in one short sentence." }
```

Response `200`: `{ "reply": "..." }`.

| Case | Response |
|---|---|
| Message is blank or longer than 2000 characters | `400` |
| The language model is unavailable, times out or rejects the call | `503` with an RFC 7807 body |

The endpoint keeps no conversation history, has no authentication yet (T-050) and has no access to events, seats or bookings. A default system prompt tells the model this, so it says it cannot check live data. Architecture: REST controller, `AgentChatService`, Spring AI `ChatClient`.

## Configuration and secrets

| Variable | Purpose | Default |
|---|---|---|
| `GROQ_API_KEY` | Groq API key. Required: the service does not start without it. | none |
| `GROQ_MODEL` | Groq model ID | `openai/gpt-oss-120b` |

Create your own key at https://console.groq.com/keys. Put it only in your local `.env` file or your terminal environment. `.env` is ignored by git and must never be committed; `.env.example` shows the variable names. The Telegram bot token will follow the same rule.

Groq retired `llama-3.3-70b-versatile` on 16 August 2026, so do not use it. Check Groq's model list when changing `GROQ_MODEL`.

## Tests

```bash
cd ai-agent-service
set -a; source .env; set +a
mvn test
```

Four tests, in `ChatApiIntegrationTest` and `ChatApiErrorHandlingTest`:

- a valid message returns `200` with a non-empty reply (this makes a **real call to Groq**, so it needs `GROQ_API_KEY` and uses a few tokens),
- a blank message returns `400`,
- a message over 2000 characters returns `400`,
- a simulated model failure returns `503` (the service is mocked, no Groq call).

The integration tests also start the application against the local `ai_db`, so run `docker compose up -d ai-db` first. In IntelliJ, set `GROQ_API_KEY` in the JUnit run configuration.

## Current status

Implemented (T-060): the skeleton, the pgvector database and the health endpoint.

Implemented (T-061): the Groq chat endpoint with validation and `503` handling, and the four tests above, which passed.

Pending: MCP tools (T-062, T-063), Telegram (T-064), RAG (T-065), safety checks (T-066).
