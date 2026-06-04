# NOW

This file is the current-session handoff. Read it after `AGENTS.md` before starting work in this repository.

The first Spaces/Accounts API slice from `PLAN.md` is implemented.

## Current focus

Change the Accounts API shape so single-account operations use direct account routes, per `docs/adr/0005-use-direct-account-routes-for-single-account-resources.md`. Keep space-scoped account list/create routes.

## Fresh-agent summary

The user found the current `/api/v1/spaces/{spaceId}/accounts/{accountId}` shape ugly because it requires two IDs for one account. After comparing Stripe/GitHub/Notion-style APIs, the agreed shape is:

```http
GET  /api/v1/spaces/{spaceId}/accounts
POST /api/v1/spaces/{spaceId}/accounts
GET  /api/v1/accounts/{accountId}
PATCH /api/v1/accounts/{accountId}
```

`acc_...` IDs are globally unique, so the account ID is enough for single-account reads/updates. Account responses should still include `spaceId`. Future auth should authorize by resolving the account's space internally, not by duplicating the space ID in the path.

Durable decision: `docs/adr/0005-use-direct-account-routes-for-single-account-resources.md` supersedes the nested single-account route part of `docs/adr/0002-use-generated-opaque-string-identifiers.md`. `PLAN.md` still contains the old nested route shape; treat ADR 0005 as the latest route decision until implementation updates docs/tests/code.

Likely implementation surfaces:

- `app/src/main/kotlin/com/jordi9/doscomas/App.kt` route registration
- `feature/planning/inbound/GetAccountHandler.kt`
- `feature/planning/inbound/PatchAccountHandler.kt`
- `feature/planning/application/GetAccountUseCase.kt`
- `feature/planning/application/PatchAccountUseCase.kt`
- `feature/planning/outbound/AccountRepository.kt`
- planning HTTP scenarios and OpenAPI route coverage tests
- `app/src/main/resources/static/openapi.yaml`

## Suggested skills

- `backend-mode`: required before Kotlin route/handler/use-case/test changes.
- `grill-with-docs`: use if route terminology or durable docs need more challenge.
- `git-committer`: only when preparing a commit.

## Validation expectation

For documentation-only edits, no build is required. After implementing route changes, run from `~/dev/doscomas-api`:

```bash
./gradlew spotlessApply
./gradlew test
./gradlew build
```
