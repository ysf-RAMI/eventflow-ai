# UML — EventFlow AI MVP

All diagram source files are standalone Mermaid `.mmd` files in this folder. Open them in a Mermaid-compatible Markdown preview or Mermaid Live Editor.

## Diagram index

| File | Content |
|---|---|
| `01-system-context.mmd` | Actors, identity, frontend, gateway, services and databases |
| `02-service-architecture.mmd` | Service boundaries, REST calls and database ownership |
| `03-domain-class-diagram.mmd` | Domain classes, enums and cross-service UUID references |
| `04-database-erd.mmd` | Tables, local foreign keys and logical cross-service references |
| `05-booking-sequence.mmd` | REST-only reservation and simulated payment sequence |
| `06-booking-state-machine.mmd` | Booking lifecycle and permitted high-level transitions |

## Modeling rules

- Each service owns its own entities and database.
- Dashed/logical links between services are UUID references, not SQL foreign keys or JPA relationships.
- Only `Seat` → `SeatHold` (Inventory) and `Booking` → `BookingItem` (Booking) are local SQL relationships.
- `Customer`, `Admin`, and `Role` are not business JPA entities; Keycloak owns identities and roles.
- Payment is simulated only. There is no Refund entity.
- No Outbox, Inbox, RabbitMQ, Kafka, or asynchronous messaging in the MVP.
- Notification and AI/RAG are later phases.
- GraphQL is future scope.

See [Java class design](../classes/CLASS_DESIGN.md) for class responsibilities and package layouts.
