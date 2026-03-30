# Room Database with Core Inventory Schema

Design and implement the Room database with core entity models: InventoryItem (barcode, name, quantity, location, category), Location (warehouse zones/bins), InventoryOperation (type, item, quantity, timestamp, operator), and OutboxEntry (pending sync queue). Include DAOs with Flow-based queries, database migrations strategy, and pre-populated test data for development.

## Rationale
Room is the local source of truth in the offline-first architecture. Every inventory operation, sync operation, and UI display depends on a well-designed local database. Without Room, no data persistence is possible.

## User Stories
- As a warehouse worker, I want my scanned inventory data to persist locally so that nothing is lost even if the app crashes or the device restarts
- As a developer, I want a well-structured database schema so that inventory operations can be built on a solid data foundation

## Acceptance Criteria
- [ ] Room database with InventoryItem, Location, InventoryOperation, and OutboxEntry entities
- [ ] DAOs expose Flow-based queries for reactive UI updates
- [ ] Foreign key relationships enforced between entities
- [ ] Database migration strategy defined (version tracking)
- [ ] Indexes on frequently queried columns (barcode, sync_status)
- [ ] Unit tests pass for all DAO operations
- [ ] Data survives app restart and process death
