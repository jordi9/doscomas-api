# NOW

The first Spaces/Accounts API slice from `PLAN.md` is implemented. Stay in post-implementation refactoring within that slice rather than starting a new feature.

## Current focus

Move past NanoID/public ID coverage (now captured in `app/src/test/kotlin/com/jordi9/doscomas/shared/domain/NanoIdTest.kt`) and tighten focused domain validation coverage for planning primitives, especially `Money` parsing/formatting and `PlanningValidation` rules.

## Next

Start with small Kotest domain tests, then only adjust HTTP scenarios if they expose a behavior gap.
