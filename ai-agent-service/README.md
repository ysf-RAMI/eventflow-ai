# ai-agent-service

The AI agent for EventFlow AI. It will answer customer questions with an LLM (Groq, through Spring AI), call the other services' read-only MCP tools, answer policy questions from indexed documents (RAG), and talk to customers through Telegram. It owns only its own database, `ai_db`, and never holds business source-of-truth data. See ADR 0003 and ADR 0005.

- Port: `8085`
- Database: `ai_db` (host port `5436`), PostgreSQL 16 with the pgvector extension
- Stack: Java 21, Spring Boot 4.1, Flyway

## Local run

From the repository root:

```bash
docker compose up -d ai-db
cd ai-agent-service
mvn spring-boot:run
```

Health: http://localhost:8085/actuator/health

## Current status

Skeleton only (T-060). The application starts, Flyway applies `V1__enable_pgvector.sql`, and the health endpoint answers. Verified manually. There are no endpoints, no LLM calls, no tools and no tests yet.

Pending: Groq chat endpoint (T-061), MCP tools (T-062, T-063), Telegram (T-064), RAG (T-065), safety checks (T-066).

## Secrets

The Groq API key and the Telegram bot token will be provided as environment variables and must never be committed.
