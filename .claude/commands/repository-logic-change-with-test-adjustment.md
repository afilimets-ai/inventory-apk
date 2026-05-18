---
name: repository-logic-change-with-test-adjustment
description: Workflow command scaffold for repository-logic-change-with-test-adjustment in inventory-apk.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /repository-logic-change-with-test-adjustment

Use this workflow when working on **repository-logic-change-with-test-adjustment** in `inventory-apk`.

## Goal

Updates repository logic and immediately adjusts or adds tests to ensure correctness, often fixing testability or compatibility issues.

## Common Files

- `app/src/main/java/com/inventory/data/repository/*.kt`
- `app/src/test/java/com/inventory/data/repository/*Test.kt`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Update repository implementation files in app/src/main/java/com/inventory/data/repository/
- Update or add corresponding test files in app/src/test/java/com/inventory/data/repository/
- Make test-specific adjustments (e.g., override methods, fix test signatures, use argument captors)

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.