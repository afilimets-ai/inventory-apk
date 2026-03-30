# Dependency Injection with Hilt

Set up Hilt as the DI framework across the application. Configure Application-scoped, Activity-scoped, and ViewModel-scoped components. Provide singleton instances for database, networking, scanner manager, and repository classes.

## Rationale
Hilt is specified in the tech stack (CLAUDE.md) and is essential for testability, modular architecture, and proper singleton lifecycle management. The NewlandScannerManager must be Application-scoped, and repositories need proper scoping for data layer isolation.

## User Stories
- As a developer, I want dependency injection so that components are loosely coupled and easily testable

## Acceptance Criteria
- [ ] Hilt configured with @HiltAndroidApp Application class
- [ ] At least one @Module providing Application-scoped dependencies
- [ ] ViewModels use @HiltViewModel annotation
- [ ] Activities annotated with @AndroidEntryPoint
- [ ] Dependency graph compiles without errors
