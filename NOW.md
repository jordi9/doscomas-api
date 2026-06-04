# NOW

This file is the current-session handoff. Read it after `AGENTS.md` before starting work in this repository.

## Current focus

The first Spaces/Accounts API slice from `PLAN.md` is implemented. Current work is post-implementation review and refactoring, mainly around planning test fixtures and scenario readability. Do not start the next `PLAN.md` slice unless the user asks.

## Fresh-agent summary

This session focused on settling the test fixture style after the first planning slice. The durable fixture decision is captured in `docs/adr/0004-use-examples-and-tables-for-test-fixtures.md`; read that instead of reconstructing the debate from scratch.

Recent refactors touched the planning fixture and scenario tests. Inspect the current diff before continuing, especially:

- `app/src/test/kotlin/com/jordi9/doscomas/fixture/PlanningExamples.kt`
- `app/src/test/kotlin/com/jordi9/doscomas/fixture/PlanningTables.kt`
- `app/src/test/kotlin/com/jordi9/doscomas/scenario/PlanningStages.kt`
- `app/src/test/kotlin/com/jordi9/doscomas/scenario/PlanningShould.kt`

The latest discussion questioned whether scenario context should keep `spaceIds`/`accountIds` lists. The current direction is to prefer named context fields when the scenario only needs semantic roles such as current, first, second, or other-space IDs.

Relevant durable artifacts:

- API slice plan: `PLAN.md`
- Domain terminology: `CONTEXT.md`
- Fixture decision: `docs/adr/0004-use-examples-and-tables-for-test-fixtures.md`
- Other planning ADRs: `docs/adr/`

## Suggested skills

- `backend-mode`: invoke before Kotlin backend/test work, including fixture or scenario refactors.
- `grill-with-docs`: useful if questioning terminology, slice boundaries, or whether a decision belongs in `CONTEXT.md`/ADRs.
- `git-committer`: invoke only when preparing a commit.

## Validation expectation

After code refactors, run:

```bash
./gradlew spotlessApply
./gradlew test
./gradlew build
```
