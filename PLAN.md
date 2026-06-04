# Spaces and Accounts API Plan

Reviewed plan for the first real Dos Comas backend API slice.

## Scope

Implement financial planning spaces and accounts in `~/dev/doscomas-api`.

Keep this slice small:

- No auth/users/memberships yet.
- No bank sync or provider integrations yet.
- No transactions, encryption, delete/archive, or account history yet.
- Keep existing skeleton `/api/v1/items` endpoints for now.

## Documentation decisions already captured

- `CONTEXT.md` — glossary for User, Space, Account, Balance, Monthly Contribution, Account Category, Account Display.
- `docs/adr/0001-money-storage-and-api-format.md` — store money as cents internally, expose decimal strings in the API.
- `docs/adr/0002-use-generated-opaque-string-identifiers.md` — generated opaque prefixed IDs.
- `docs/adr/0003-model-account-display-separately.md` — display details are separate from core account data.
- `docs/adr/0005-use-direct-account-routes-for-single-account-resources.md` — single-account reads and updates use direct account routes.

## Routes

Use path-versioned API routes. Keep `/api/v1`; do not introduce header-based versioning yet.

Spaces:

```http
GET  /api/v1/spaces
POST /api/v1/spaces
GET  /api/v1/spaces/{spaceId}
```

Account list/create operations stay scoped to a space. Single-account reads and updates use the globally unique account ID directly:

```http
GET   /api/v1/spaces/{spaceId}/accounts
POST  /api/v1/spaces/{spaceId}/accounts
GET   /api/v1/accounts/{accountId}
PATCH /api/v1/accounts/{accountId}
```

Do not implement top-level account list/create routes yet. There is no current/default space until auth exists.

## Package structure

Implement under one planning feature:

```text
feature/planning/
  domain/
  application/
  inbound/
  outbound/
```

Use the existing `feature/item` slice as the structural reference.

Shared ID generation should live in:

```text
shared/domain/PublicIdGenerator.kt
```

## Identifiers

Use generated opaque string IDs:

```text
sp_...
acc_...
```

Rules:

- IDs are generated server-side.
- Clients do not provide IDs in create requests.
- Duplicate names are allowed.
- IDs are stable and immutable.
- Use separate value types: `SpaceId`, `AccountId`.
- Invalid ID prefix/format returns `400`.
- Well-formed missing resource returns `404`.

Use a tiny dependency-free NanoID-style generator implemented in the backend. Use a URL-safe alphabet and inject the concrete generator through the registry/factory wiring. Tests should use a deterministic seeded generator.

## Space model

First slice `Space` fields:

```text
id
name
createdAt
updatedAt
```

Rules:

- `POST /api/v1/spaces` accepts `name` only.
- Trim names and reject blank names.
- `GET /api/v1/spaces` returns all spaces until auth exists.
- `GET /api/v1/spaces/{spaceId}` returns only the space resource, not embedded accounts.

## Account model

Core account fields:

```text
id
spaceId
name
category
balance
monthlyContribution
currency
note
balanceUpdatedAt
createdAt
updatedAt
display
```

Rules:

- `spaceId` comes from the route, not the JSON request body.
- `POST /api/v1/spaces/{spaceId}/accounts` requires `name`, `category`, and `balance`.
- `monthlyContribution` defaults to `"0.00"`.
- `currency` is EUR only for now; omitted means EUR.
- `balance` must be non-negative.
- `monthlyContribution` may be negative.
- `balanceUpdatedAt` defaults to now on create.
- If `balance` changes in PATCH, update `balanceUpdatedAt` to now.
- Any core/display change updates account `updatedAt`.
- Account responses include `spaceId` even though routes are nested.

## Account categories

Use fixed English enum values:

```text
cash
investment
private_pension
real_estate
social_security
other
```

Spanish labels remain frontend/product copy, not backend enum values.

## Money format

API fields are decimal strings:

```json
{
  "balance": "8420.00",
  "monthlyContribution": "-353.00",
  "currency": "EUR"
}
```

DB stores normalized integer cents:

```text
balance_cents INTEGER NOT NULL
monthly_contribution_cents INTEGER NOT NULL
```

Rules:

- Accept money strings with 0 to 2 decimal places: `"8420"`, `"8420.0"`, `"8420.00"`.
- Reject more than two decimal places.
- Reject JSON numbers for money; use strings.
- Always return exactly two decimal places.
- Do not use floating point for money.
- Future provider values with more precision belong in a separate raw provider snapshot model, not this account table.

## Account display

Account display is separate from core financial account data, but composed into account API responses.

Display fields:

```text
initials
color
typeLabel
subtitle
```

Rules:

- Store display in a separate `account_displays` table.
- Do not store display as top-level account columns.
- Do not store arbitrary display JSON in this slice.
- `display` may be omitted or partial on create.
- Empty display response is `"display": {}`.
- `color` accepts `#RRGGBB` only.
- `account_displays` has no own timestamps.
- Display changes update the parent account `updatedAt`.
- No separate display endpoints yet.

## PATCH semantics

Use ordinary `application/json` partial objects, not `application/merge-patch+json`.

Rules:

- Missing fields are unchanged.
- Explicit `null` clears nullable fields, including `note` and display keys.
- Explicit `null` for non-nullable fields returns `400`.
- If `display` is omitted, display is unchanged.
- If `display: null`, clear all display.
- If included display keys are `null`, clear those keys.
- If all display fields are cleared, delete the `account_displays` row.
- Success returns `200` with the full updated account response.

Editable account fields:

```text
name
category
balance
monthlyContribution
currency
note
display
```

Server-owned fields:

```text
id
spaceId
createdAt
updatedAt
balanceUpdatedAt, except automatic update when balance changes
```

## Database schema

Use simple single-column surrogate IDs, not composite primary keys.

```sql
CREATE TABLE spaces (
  id          TEXT    PRIMARY KEY,
  name        TEXT    NOT NULL,
  created_at  INTEGER NOT NULL,
  updated_at  INTEGER NOT NULL
);

CREATE TABLE accounts (
  id                            TEXT    PRIMARY KEY,
  space_id                      TEXT    NOT NULL REFERENCES spaces(id),
  name                          TEXT    NOT NULL,
  category                      TEXT    NOT NULL,
  balance_cents                 INTEGER NOT NULL,
  monthly_contribution_cents    INTEGER NOT NULL,
  currency                      TEXT    NOT NULL,
  note                          TEXT,
  balance_updated_at            INTEGER NOT NULL,
  created_at                    INTEGER NOT NULL,
  updated_at                    INTEGER NOT NULL
);

CREATE TABLE account_displays (
  account_id  TEXT PRIMARY KEY REFERENCES accounts(id),
  initials    TEXT,
  color       TEXT,
  type_label  TEXT,
  subtitle    TEXT,
  CHECK (initials IS NOT NULL OR color IS NOT NULL OR type_label IS NOT NULL OR subtitle IS NOT NULL)
);
```

Declare foreign keys in the schema, but also check space/account existence in application/repository code because SQLite FK enforcement depends on connection-level configuration.

Store timestamps as epoch milliseconds in the DB and expose ISO-8601 `Instant.toString()` strings in the API.

## Validation and errors

Reuse the current error response shape:

```json
{
  "error": "..."
}
```

Validation rules:

- Trim names and reject blank names.
- Apply practical max lengths:
  - name: 120
  - note/subtitle/typeLabel: 500
  - initials: 8
  - color: 7
- Reject invalid account categories.
- Reject invalid money format/precision.
- Reject negative balances.
- Reject non-EUR currencies for now.
- Reject malformed `sp_...` / `acc_...` IDs with `400`.
- Return `404` when a valid-looking resource is missing.
- If an account exists in another space but not under the requested `spaceId`, return `404`.

## OpenAPI

Update `app/src/main/resources/static/openapi.yaml` with all new routes, request schemas, response schemas, and validation notes.

Existing OpenAPI route coverage tests must keep passing.

## Tests

Follow the `backend-mode` testing guidance before implementation. Use Canon TDD:

1. Write the scenario list.
2. Turn one scenario into a failing test.
3. Make it pass minimally.
4. Refactor while keeping tests green.
5. Repeat until the list is empty.

Use `krat-kogiven` scenario tests following the existing `ItemShould` / `ItemStages` style:

- `ScenarioStringSpec` from `com.jordi9.kogiven`
- `StageContext`-based Given/When/Then stages
- real Ktor test app
- in-memory SQLite
- HTTP requests through the public API
- exact JSON/status assertions where practical

Do not replace this with repository-only or use-case-only tests for the first slice; backend-mode prefers HTTP component scenarios for API behavior.

Minimum scenarios:

- list spaces returns empty list initially
- create a space
- get a space by ID
- create an account in a space
- list accounts scoped to a space
- get account scoped to a space
- account in another space returns `404`
- patch core account fields
- patch updates balance and `balanceUpdatedAt`
- patch display fields
- clear display fields and return `display: {}`
- clear nullable note
- reject invalid money precision
- reject negative balance
- reject invalid category
- reject invalid ID prefix/format
- reject account creation for missing space
- OpenAPI route coverage still passes

## Build commands before finishing implementation

Run from `~/dev/doscomas-api`:

```bash
./gradlew spotlessApply
./gradlew test
./gradlew build
```

If the project uses `ktlintFormat` instead of `spotlessApply` in the final implementation workflow, run the formatter that is actually wired and keep the build green.
