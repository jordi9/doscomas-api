# CLAUDE.md

Doscomas: Kotlin REST API backend using Ktor and hexagonal architecture.

## MANDATORY: Use backend-mode Skill

**BEFORE implementing, planning, or discussing architecture: invoke the `backend-mode` skill.**

This skill defines how this project works: layer placement, TDD workflow, testing strategy, and patterns. Do not guess or assume - read the skill first.

## Build Commands

```bash
./gradlew spotlessApply  # Format code (always run first)
./gradlew build          # Build + test
./gradlew test           # Run tests
./gradlew run            # Run application
```

## Reminders

- Update `app/src/main/resources/static/openapi.yaml` when adding/changing handlers or DTOs.
- Migrations: `app/src/main/resources/db/migration/V{version}__{description}.sql`
