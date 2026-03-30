# Reliable Background Sync Engine

Build the sync engine using WorkManager: periodic connectivity-aware drain of the outbox queue, delta sync with sync_token for efficient catalog downloads (only changes since last sync), exponential backoff with jitter on failures, and idempotency key headers for duplicate-safe retries. The sync engine is the bridge between offline operations and server state.

## Rationale
WorkManager guarantees sync execution even after app restart or device reboot. Delta sync with sync_token prevents re-downloading the entire catalog on every sync (bandwidth matters in warehouses with poor connectivity). Exponential backoff with jitter prevents thundering herd when 50-200 devices reconnect simultaneously. Idempotency keys ensure no duplicate operations on retry. This addresses the core reliability gap that competitors like Sortly (pain-5-3) and RFgen (pain-6-3) struggle with.

## User Stories
- As a warehouse worker, I want my pending operations to sync automatically when I walk back into WiFi range so that I don't have to remember to sync manually
- As an IT administrator, I want the sync to handle network failures gracefully so that devices don't create duplicate entries or lose data

## Acceptance Criteria
- [ ] WorkManager worker drains outbox entries in FIFO order when connectivity available
- [ ] Each outbox entry sends idempotency_key header to prevent server-side duplicates
- [ ] Successful sync updates outbox entry status to 'synced'
- [ ] Failed sync increments retry_count, applies exponential backoff (base 2s, max 5min) with jitter
- [ ] Delta sync downloads catalog changes using sync_token (server returns only records modified since token)
- [ ] sync_token persisted in SharedPreferences/DataStore between sessions
- [ ] Sync works after app restart and device reboot (WorkManager persistence)
- [ ] Sync respects battery constraints (no sync below 15% battery)
- [ ] Sync progress observable via Flow for UI status indicator
- [ ] 95%+ success rate for sync operations under normal connectivity
