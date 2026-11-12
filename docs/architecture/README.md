# Architecture

## Container view (C4)

```mermaid
flowchart TB
    UI["Angular Web App"] --> GW["API Gateway (Spring Cloud)"]
    TG["Telegram Bot"] --> AG["AI Agent Service"]
    GW --> AG
    GW --> BK["Booking Service (Saga orchestrator)"]
    GW --> CA["Catalog & Inventory Service"]
    AG --> RAG["RAG pipeline"]
    RAG --> VD[("pgvector: policies and FAQs")]
    AG -->|controlled tools| BK
    AG -->|read-only tools| CA
    BK -->|atomic seat hold| CA
    BK --> BDB[("PostgreSQL: bookings")]
    CA --> IDB[("PostgreSQL: inventory")]
    BK <--> MQ{"RabbitMQ"}
    MQ <--> PAY["Payment Service (simulator)"]
    MQ --> NT["Notification Service"]
    PAY --> PDB[("PostgreSQL: payments")]
    NT --> TGAPI["Telegram Bot API"]
```

Each business service owns its database. There are no cross-service foreign keys.

## Reservation state machine

```mermaid
stateDiagram-v2
    [*] --> Pending
    Pending --> Held: seat acquired
    Pending --> Failed: unavailable
    Held --> Confirmed: payment success and hold still valid
    Held --> Expired: timeout
    Held --> Cancelled: customer cancels
    Held --> Failed: payment failure
    Confirmed --> Cancelled: approved cancellation
    Expired --> Refunded: late payment compensated
    Cancelled --> Refunded: refund applicable
```

## Booking sequence (happy path and compensation)

```mermaid
sequenceDiagram
    participant C as Customer
    participant G as API Gateway
    participant B as Booking Service
    participant I as Inventory Service
    participant M as RabbitMQ
    participant P as Payment Service

    C->>G: POST /bookings (event, seats)
    G->>B: create booking
    B->>I: POST /holds (atomic seat hold)
    I-->>B: hold created, with expiry
    B->>M: PaymentRequested (via outbox)
    M->>P: PaymentRequested
    P-->>M: PaymentSucceeded or PaymentFailed
    M->>B: payment result
    alt success and hold still valid
        B->>I: confirm seats
        B->>M: BookingConfirmed
    else failure or hold expired
        B->>I: release hold
        B->>M: compensate (refund if charged)
    end
```
