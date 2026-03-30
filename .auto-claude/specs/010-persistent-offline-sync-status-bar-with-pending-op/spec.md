# Persistent offline/sync status bar with pending-operations counter

## Overview
Display a persistent, non-intrusive status indicator showing network connectivity state and sync queue depth. When offline, show an amber bar with 'Offline — N changes pending sync'. When syncing, show a blue bar with progress animation. When online and synced, collapse to a small green dot. The POC has zero connectivity awareness.


## Rationale

CLAUDE.md defines an offline-first architecture with outbox pattern and WorkManager sync. Users working in warehouses frequently move between areas with good WiFi, spotty coverage, and dead zones (metal shelving, cold rooms). Without visible sync status, workers don't know if their scans are being saved only locally, if they need to move to a better coverage area before shift end, or if a sync error is silently losing data. This is a trust and data-integrity issue — workers must trust that their scans are captured even without network.

---
*This spec was created from ideation and is pending detailed specification.*
