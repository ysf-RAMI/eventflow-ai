# catalog-service

Owns event creation, update, publication, search, and event details. It does not own seats, bookings, or users.

- Port: `8081`
- Database: `catalog_db` (host port `5432`)
- Stack: Java 21, Spring Boot 3.3, PostgreSQL 16, Flyway
- API interfaces: REST for application/admin operations; read-only MCP tools for the future AI agent; GraphQL later.

## Event model

`Event` fields: `id`, `createdBy`, `title`, `description`, `date`, `status`.

- `date`: Java `LocalDateTime`, stored in SQL as `event_date`.
- `createdBy`: Keycloak JWT subject of the admin.
- `EventStatus`: `DRAFT`, `PUBLISHED`, `CANCELLED`, `COMPLETED`.
- No venue fields, starts/ends pair, cancellation policy, or event audit timestamps in the approved MVP model.

## Java structure

```text
 domain/Event.java
 domain/EventStatus.java
 repository/EventRepository.java
 service/EventService.java
 controller/rest/EventRestController.java
 controller/mcp/EventMcpTools.java
 dto/CreateEventRequest.java       (record)
 dto/UpdateEventRequest.java       (record)
 dto/EventResponse.java            (record)
```

`EventMcpTools` is an MCP tool component, not an MVC REST controller. MCP tools are read-only. Admin CRUD uses REST and requires `ADMIN`.

## Database

Initial schema: `src/main/resources/db/migration/V1__create_events.sql`.

## Status

Spring Boot application, Event entity, EventStatus enum, repository, Flyway migration, and PostgreSQL configuration are present. REST controllers, Keycloak enforcement, DTO records, and MCP tools remain planned.
