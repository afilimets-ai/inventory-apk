# Offline Sync Status Indicator

Display a persistent, non-intrusive status bar showing: connectivity state (online/offline), pending outbox operations count, last successful sync timestamp, and active sync progress. Color-coded: amber when offline with pending changes, blue when actively syncing, green when fully synced, red on sync error. Collapsible to minimal indicator when synced.

## Rationale
Workers moving between areas with WiFi and dead zones need to trust that their scans are being captured. Without visible sync status, they can't tell if data is local-only, syncing, or fully backed up. This is a trust and data-integrity signal. Sortly users complain about having to restart the app to resolve sync issues (pain-5-3) — proactive status prevents this anxiety.

## User Stories
- As a warehouse worker, I want to see at a glance whether my scans have been synced so that I know my data is backed up before ending my shift
- As a warehouse supervisor, I want to see sync status on worker devices so that I can identify connectivity problems in specific warehouse zones

## Acceptance Criteria
- [ ] Status bar visible at top of every screen
- [ ] Shows 'Offline — N changes pending' in amber when offline with queued operations
- [ ] Shows 'Syncing...' with progress animation in blue during active sync
- [ ] Shows green indicator when online and fully synced
- [ ] Shows red indicator with error message on persistent sync failures
- [ ] Last sync timestamp displayed (e.g., 'Last sync: 2 min ago')
- [ ] Bar is non-intrusive — doesn't block content interaction
- [ ] Tapping the bar shows detailed sync status (expandable)
