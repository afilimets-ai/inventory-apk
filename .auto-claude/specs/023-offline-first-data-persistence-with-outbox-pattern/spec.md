# Offline-First Data Persistence with Outbox Pattern

Implement the outbox pattern for guaranteed offline data capture: every inventory operation writes both the business record AND an outbox entry in a single Room ACID transaction. The outbox table tracks sync status (pending/syncing/synced/failed), retry count, and created timestamp. Operations are never lost — they exist locally until confirmed synced by the server.

## Rationale
This is the architectural foundation of offline-first. Competitors like Sortly require full app restart to resolve sync issues (pain-5-3), 1C has no offline mobile capability at all (pain-4-4), and even RFgen shows inconsistent sync performance (pain-6-3). True ACID outbox pattern — not 'cloud-first with offline fallback' — is what fills market gap-3.

## User Stories
- As a warehouse worker in a dead zone, I want my scans to be saved locally so that no data is lost when I'm offline
- As a warehouse supervisor, I want to know how many operations are pending sync so that I can assess data freshness

## Acceptance Criteria
- [ ] Business record + outbox entry written in single Room @Transaction
- [ ] Transaction is ACID — partial write is impossible
- [ ] OutboxEntry tracks: id, operation_type, payload, status, retry_count, created_at, idempotency_key
- [ ] Each outbox entry has a UUID idempotency key generated at creation time
- [ ] Operations recorded offline are visible in operation history immediately
- [ ] Outbox queue depth is queryable (for sync status indicator)
- [ ] Failed sync entries remain in outbox for retry (not deleted)
- [ ] App functions fully with airplane mode enabled — no crashes, no missing features
