# AGENTS.md

Doscomas: Kotlin REST API backend using Ktor and hexagonal architecture.

**BEFORE implementing, planning, or discussing architecture: invoke the `backend-mode` skill.**

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
- Don't use `internal` in classes.
