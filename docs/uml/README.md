# UML: use cases and class diagram

Draft for review. Requirement references point to docs/requirements/SRS.md.

## Use case diagram

```mermaid
flowchart LR
    CU["Customer"]
    OR["Organizer (admin)"]
    AG["AI Agent"]
    PS["Payment Simulator"]

    subgraph SYS["EventFlow AI"]
        UC1(["Browse and search events (FR-001)"])
        UC2(["Ask policy question (FR-012)"])
        UC3(["Check seat availability (FR-002)"])
        UC4(["Hold seats (FR-003)"])
        UC5(["Pay for booking (FR-005)"])
        UC6(["Cancel booking (FR-008)"])
        UC7(["View booking status (FR-010)"])
        UC8(["Receive notifications (FR-009)"])
        UC9(["Create event and seat map (FR-011)"])
        UC10(["Publish event (FR-011)"])
        UC11(["Refund booking (FR-008)"])
        UC12(["Release expired hold (FR-007)"])
    end

    CU --> UC1
    CU --> UC2
    CU --> UC3
    CU --> UC4
    CU --> UC5
    CU --> UC6
    CU --> UC7
    CU --> UC8
    OR --> UC9
    OR --> UC10
    AG --> UC1
    AG --> UC2
    AG --> UC3
    AG -->|validated parameters| UC4
    AG -.->|needs customer confirmation| UC6
    PS --> UC5
    UC5 -.->|include| UC4
    UC6 -.->|include| UC11
    UC12 -.->|extend when hold times out| UC4
```

The AI agent can hold seats and request cancellation only through controlled tools. Cancellation requires explicit customer confirmation. The agent has no path to payment confirmation or refunds.

## Class diagram

```mermaid
classDiagram
    direction LR

    class Customer {
        +UUID id
        +String name
        +String email
        +String telegramChatId
    }

    class Event {
        +UUID id
        +String title
        +String venue
        +LocalDateTime startsAt
        +EventStatus status
    }

    class Seat {
        +UUID id
        +String label
        +String section
        +long priceCents
        +SeatStatus status
        +long version
        +hold() void
        +release() void
        +sell() void
    }

    class Booking {
        +UUID id
        +BookingStatus status
        +long totalCents
        +Instant createdAt
        +confirm() void
        +cancel() void
        +expire() void
    }

    class BookingItem {
        +UUID id
        +long priceCents
    }

    class SeatHold {
        +UUID id
        +Instant expiresAt
        +HoldStatus status
    }

    class Payment {
        +UUID id
        +String idempotencyKey
        +long amountCents
        +PaymentStatus status
        +String providerReference
    }

    class Refund {
        +UUID id
        +long amountCents
        +RefundStatus status
    }

    class OutboxEvent {
        +UUID id
        +String type
        +String payload
        +boolean published
        +Instant createdAt
    }

    class PolicyDocument {
        +UUID id
        +String title
        +String content
        +vector embedding
    }

    class BookingStatus {
        <<enumeration>>
        PENDING
        HELD
        CONFIRMED
        FAILED
        EXPIRED
        CANCELLED
        REFUNDED
    }

    class SeatStatus {
        <<enumeration>>
        AVAILABLE
        HELD
        SOLD
    }

    class HoldStatus {
        <<enumeration>>
        ACTIVE
        CONFIRMED
        RELEASED
        EXPIRED
    }

    class PaymentStatus {
        <<enumeration>>
        PENDING
        SUCCEEDED
        FAILED
    }

    Customer "1" --> "0..*" Booking : places
    Event "1" --> "1..*" Seat : contains
    Event "1" --> "0..*" Booking : for
    Event "0..1" --> "0..*" PolicyDocument : governed by
    Booking "1" *-- "1..*" BookingItem : includes
    BookingItem "*" --> "1" Seat : reserves
    Booking "1" --> "1..*" SeatHold : holds
    SeatHold "*" --> "1" Seat : locks
    Booking "1" --> "0..*" Payment : paid by
    Payment "1" --> "0..1" Refund : refunded by
    Booking ..> OutboxEvent : publishes
```

## Invariants

- At most one ACTIVE hold exists per seat. The database enforces this with a conditional update and row locking. `version` supports optimistic locking.
- `Payment.idempotencyKey` is unique, so a repeated request cannot create a second charge.
- `OutboxEvent` is written in the same transaction as the booking change. This prevents "DB updated, event lost".
- `PolicyDocument` belongs to the AI and RAG side. Booking and inventory never read it.
