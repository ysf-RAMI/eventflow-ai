# notification-service — deferred

Notification delivery is explicitly deferred and is not part of the MVP implementation. No broker consumer, Telegram bot, notification database, or notification dependency is included in the current workflow.

When introduced later, notifications should use REST initially and must never determine booking/payment correctness. A failed notification must not roll back a confirmed booking.
