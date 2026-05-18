```markdown
# inventory-apk Development Patterns

> Auto-generated skill from repository analysis

## Overview

This skill provides a comprehensive guide to the development patterns, coding conventions, and workflows used in the `inventory-apk` repository. The codebase is written in Kotlin and focuses on inventory management features, including data import/export, repository logic, and robust test coverage. The repository emphasizes test-driven development (TDD), clear code organization, and adherence to conventional commit standards.

## Coding Conventions

- **File Naming:**  
  Use PascalCase for Kotlin files.  
  _Example:_  
  ```
  CatalogImportManager.kt
  ProductRepository.kt
  ```

- **Import Style:**  
  Use relative imports within the package structure.  
  _Example:_  
  ```kotlin
  import com.inventory.sync.serializer.CsvSerializer
  import com.inventory.data.repository.ProductRepository
  ```

- **Export Style:**  
  Use named exports (Kotlin's default).  
  _Example:_  
  ```kotlin
  class CatalogImportManager { ... }
  ```

- **Commit Messages:**  
  Follow [Conventional Commits](https://www.conventionalcommits.org/) with prefixes like `feat`, `fix`, and `docs`.  
  _Example:_  
  ```
  feat: add Excel serializer with preview support
  fix: correct transaction rollback in ProductRepository
  docs: update README with import instructions
  ```

## Workflows

### Feature Implementation with TDD
**Trigger:** When adding a new feature or module that requires robust test coverage  
**Command:** `/new-feature-tdd`

1. Create or update implementation files in `app/src/main/java/...`.
2. Create or update corresponding test files in `app/src/test/java/...`.
3. Iterate between implementation and tests until the feature is complete.

_Example:_
```kotlin
// app/src/main/java/com/inventory/sync/catalogimport/CatalogImportManager.kt
class CatalogImportManager { ... }

// app/src/test/java/com/inventory/sync/catalogimport/CatalogImportManagerTest.kt
class CatalogImportManagerTest { ... }
```

---

### Repository Logic Change with Test Adjustment
**Trigger:** When updating repository logic and ensuring tests remain valid  
**Command:** `/update-repository-with-tests`

1. Update repository implementation files in `app/src/main/java/com/inventory/data/repository/`.
2. Update or add corresponding test files in `app/src/test/java/com/inventory/data/repository/`.
3. Make test-specific adjustments (override methods, fix signatures, use argument captors).

_Example:_
```kotlin
// app/src/main/java/com/inventory/data/repository/ProductRepository.kt
class ProductRepository { ... }

// app/src/test/java/com/inventory/data/repository/ProductRepositoryTest.kt
@Test
fun testTransactionRollback() { ... }
```

---

### Serializer Extension with Tests
**Trigger:** When adding or improving data import/export formats and ensuring they are tested  
**Command:** `/extend-serializer`

1. Update or create serializer implementation files in `app/src/main/java/com/inventory/sync/serializer/`.
2. Update or create corresponding test files in `app/src/test/java/com/inventory/sync/serializer/`.

_Example:_
```kotlin
// app/src/main/java/com/inventory/sync/serializer/ExcelSerializer.kt
class ExcelSerializer { ... }

// app/src/test/java/com/inventory/sync/serializer/ExcelSerializerTest.kt
class ExcelSerializerTest { ... }
```

---

### Test Fix for Compatibility or Mocking
**Trigger:** When fixing test code for framework compatibility or to enable mocking  
**Command:** `/fix-test-compat`

1. Identify test compatibility or mocking issue.
2. Update test files to fix signatures, use argument captors, or override methods as needed.

_Example:_
```kotlin
// app/src/test/java/com/inventory/data/repository/ProductRepositoryTest.kt
@Before
fun setUp() {
    MockitoAnnotations.initMocks(this)
}
```

## Testing Patterns

- **Test File Location:**  
  Place test files alongside their corresponding implementation, using the pattern `*Test.kt` in `app/src/test/java/...`.

- **Test Framework:**  
  While the specific framework is not detected, conventions suggest use of JUnit (likely JUnit 4).

- **Test Adjustments:**  
  Common adjustments include fixing method signatures, using argument captors for mocks, and overriding methods for testability.

_Example:_
```kotlin
// app/src/test/java/com/inventory/sync/serializer/CsvSerializerTest.kt
@Test
fun testParseRaw_validCsv_returnsExpectedData() { ... }
```

## Commands

| Command                    | Purpose                                                                 |
|----------------------------|-------------------------------------------------------------------------|
| /new-feature-tdd           | Start a new feature/module with TDD workflow                            |
| /update-repository-with-tests | Update repository logic and ensure tests remain valid                  |
| /extend-serializer         | Add or improve serializer logic with corresponding tests                 |
| /fix-test-compat           | Fix or adjust test code for compatibility or mocking issues              |
```
