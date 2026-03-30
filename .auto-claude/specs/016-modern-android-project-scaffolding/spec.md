# Modern Android Project Scaffolding

Complete rewrite from legacy Java/Android Support Library POC to production-grade Kotlin project. AGP 8.x, compileSdk 34+, AndroidX, MVVM architecture with ViewModel + StateFlow, proper package structure (data/domain/presentation layers), ProGuard/R8 configuration, and .editorconfig for code standards.

## Rationale
The existing POC is a non-production Java prototype with AGP 3.2.1 and compileSdk 28. No other feature can be built without this foundational rewrite. Every known gap in the current state depends on this modernization.

## User Stories
- As a developer, I want a modern Kotlin/AndroidX project structure so that I can build features efficiently using current best practices
- As the project owner, I want the codebase to be maintainable and scalable so that I can onboard contributors and scale to multiple clients

## Acceptance Criteria
- [ ] Project compiles with Kotlin 1.9+, AGP 8.x, compileSdk 34+
- [ ] Package structure follows data/domain/presentation layer separation
- [ ] MVVM architecture with ViewModel and StateFlow in place
- [ ] ProGuard/R8 configured for release builds
- [ ] .editorconfig defines formatting standards
- [ ] App installs and launches on Newland MT90 (Android 10/11)
- [ ] Legacy Java code and Support Library dependencies removed
