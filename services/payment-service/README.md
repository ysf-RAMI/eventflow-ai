# payment-service

Payment simulator (success, decline, timeout, delayed confirmation, duplicate callbacks), idempotency keys. Own database payment_db on port 5435. Owner: Role 2. Planned: T-031, T-032.

Status: not started. Build with Spring Boot 3.3, Java 21, following the inventory-service layout. Each service has its own database and no shared tables.
