# API Client & Networking Layer

Set up Retrofit + OkHttp as the HTTP client layer for backend communication. Implement base URL configuration (per-flavor), authentication interceptor (token-based auth), request/response logging (debug only), timeout configuration, connectivity detection, and a generic API response wrapper with error handling. This is the transport layer that the sync engine uses to push outbox entries and pull catalog updates.

## Rationale
Retrofit + OkHttp is specified in the tech stack. The networking layer must support per-flavor base URLs (critical for white-label Phase 2), idempotency key headers, and resilient error handling. It bridges the local Room database with the remote backend.

## User Stories
- As a developer, I want a configured networking layer so that I can build API integrations without boilerplate setup each time
- As the app, I need to detect connectivity status so that sync operations are only attempted when a network is available

## Acceptance Criteria
- [ ] Retrofit client configured with OkHttp, JSON converter, and coroutine call adapter
- [ ] Base URL configurable per build flavor (BuildConfig field)
- [ ] Auth interceptor attaches bearer token to requests
- [ ] Request logging enabled in debug builds only
- [ ] Connectivity check utility (NetworkMonitor) using ConnectivityManager
- [ ] Generic ApiResult<T> wrapper for success/error/network-error states
- [ ] Timeouts configured: connect 10s, read 30s, write 30s
- [ ] Idempotency-Key header included in all POST/PUT requests
