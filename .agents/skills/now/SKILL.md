---
name: now
description: > 
  Maintain NOW.md as the current-session handoff. Use when the user asks to continue, refresh, or start a new work focus; supports modes: continue, next, and new.
---

# NOW Handoff Skill

Maintain `NOW.md` so a fresh agent can continue the repository work predictably.

## Arguments

The first argument must be one of:

- `continue`
- `next`
- `new`

If the mode is missing or unclear, ask the user which mode they want. Do not guess.

## General rules

- Work from the repository root.
- Read `AGENTS.md` and the current `NOW.md` before changing it.
- Inspect current repository state with `git status --short` and, when useful, targeted `git diff`.
- Do not duplicate durable content already captured in artifacts such as PRDs, plans, ADRs, issues, commits, or diffs. Reference them by path or URL instead.
- Keep `NOW.md` concise and action-oriented. It is a handoff, not a full transcript.
- Preserve validation expectations when they remain relevant.
- Do not run build/test commands for `NOW.md`-only edits unless the user asks.

## Mode: `continue`

Use when the next session is expected to continue the same surface area or refactoring direction.

Update `NOW.md` as a rich fresh-agent handoff:

1. Current focus.
2. Fresh-agent summary of the current conversation/work state.
3. Relevant changed files or files to inspect.
4. References to durable artifacts by path/URL.
5. `Suggested skills` section.
6. Validation expectation.

The `Suggested skills` section should recommend project-relevant skills, for example:

- `backend-mode`: before Kotlin backend/test work.
- `grill-with-docs`: when questioning terminology, slice boundaries, or documentation decisions.
- `git-committer`: only when preparing a commit.

## Mode: `next`

Use when the current focus remains the same but the handoff only needs a lightweight refresh.

Update `NOW.md` without adding a long fresh-agent summary:

1. Keep the same current focus.
2. Refresh immediate next step(s), changed files, and validation status if known.
3. Keep or lightly update `Suggested skills` if present.
4. Reference existing artifacts instead of restating them.

Prefer this mode for small progress updates within the same thread of work.

## Mode: `new`

Use when the user wants to choose a new feature, slice, or work direction.

Start a planning/grilling session before rewriting `NOW.md`:

1. Read the relevant durable artifacts first, usually `PLAN.md`, `CONTEXT.md`, and `docs/adr/`.
2. If available, invoke/read the `grill-with-docs` skill before discussing the new direction.
3. Ask clarifying questions when the next focus is ambiguous.
4. Do not replace the old focus in `NOW.md` until the new focus has crystallized.
5. Once decided, update `NOW.md` with the new current focus, references, suggested skills, and validation expectations.

## Suggested `NOW.md` structure

Use this structure unless the existing file has a better one:

```markdown
# NOW

This file is the current-session handoff. Read it after `AGENTS.md` before starting work in this repository.

## Current focus

...

## Fresh-agent summary

...

## Suggested skills

- ...

## Validation expectation

...
```

For `next`, omit `Fresh-agent summary` if it would only repeat the existing focus.
