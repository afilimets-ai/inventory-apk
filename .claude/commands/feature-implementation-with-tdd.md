---
name: feature-implementation-with-tdd
description: Workflow command scaffold for feature-implementation-with-tdd in inventory-apk.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /feature-implementation-with-tdd

Use this workflow when working on **feature-implementation-with-tdd** in `inventory-apk`.

## Goal

Implements a new feature by first creating data models and logic, then writing corresponding tests in a test-driven development (TDD) style.

## Common Files

- `app/src/main/java/com/inventory/sync/catalogimport/*.kt`
- `app/src/main/java/com/inventory/sync/serializer/*.kt`
- `app/src/test/java/com/inventory/sync/catalogimport/*Test.kt`
- `app/src/test/java/com/inventory/sync/serializer/*Test.kt`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Create or update implementation files in app/src/main/java/...
- Create or update corresponding test files in app/src/test/java/...
- Iterate between implementation and tests until feature is complete

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.