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
- Kogiven scenario chains: keep `.and()` and the following backticked stage call together, e.g. ``.and().`next assertion`()`` instead of putting `.and()` on its own line.
- Test spec class declarations: keep the superclass constructor on the same line when it fits, e.g. `class PlanningShould : ScenarioStringSpec<...>({` instead of breaking after `:`.
