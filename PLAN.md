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

## Architecture review follow-up

The Spaces and Accounts behavior is implemented, but the next cleanup slice should improve DDD, clean code, and hexagonal boundaries without changing the public API contract.

### 1. Make planning invariants explicit domain types

Introduce semantic value objects/types so `Space` and `Account` are always-valid at construction time:

- `SpaceName` and `AccountName`: constructors validate non-blank, maximum 120 characters, and already-normalized values.
- `Balance`: wraps account balance cents and enforces non-negative values.
- `MonthlyContribution`: wraps monthly contribution cents and allows negative values.
- `Currency`: domain enum with `EUR` as the only supported value for now.
- Defer `AccountNote`; keep `String?` plus the current 500-character validation until the core invariant refactor is complete.

Boundary rules for this refactor:

- Inbound adapters trim raw JSON names before constructing `SpaceName` or `AccountName`.
- Value object `init` blocks guard invariants directly; do not hide normalization in constructors.
- Application commands receive normalized domain value objects, not raw request strings.
- Domain entities use the value objects directly instead of primitive `String`/generic `Money` fields where the concepts differ.
- Do not add retroactive SQLite `CHECK` migrations in this slice; make the domain/application boundary correct first.

### 2. Remove PATCH command shape from the domain

`AccountUpdate` and its field-specific variants currently model PATCH/application field presence in `domain/` and are also consumed by outbound SQL code. Move that command shape out of the domain.

Preferred direction:

- Inbound keeps HTTP PATCH semantics: missing means unchanged, explicit `null` clears nullable fields, and invalid nulls return `400`.
- Application owns the normalized update command.
- Domain exposes valid account concepts and/or behavior, not HTTP field-presence details.
- Repository persistence should not depend on domain classes whose only reason to exist is PATCH transport semantics.

A simple implementation option is to load the current `Account`, apply normalized changes in application/domain code, then persist the updated account row. Prefer clarity over clever dynamic SQL until performance needs prove otherwise.

If keeping column-level updates temporarily, collapse the old display-specific split: `display_json` is now a normal account column, so `AccountCoreUpdate`, `AccountCoreWrite`, `AccountField`, `AccountDisplayRecord`, and `UPDATE_DISPLAY` should not survive as separate machinery.

### 3. Simplify update not-found handling

`UpdateAccountUseCase` currently checks `accounts.exists(accountId)` before calling `accounts.update(...)`, then handles `null` again. Remove the pre-check and let the update/load path be the single source of truth for `404` handling.

### 4. Fix hexagonal boundary leaks

`feature/greeting/application/GetGreetingUseCase.kt` imports `feature.greeting.inbound.GreetingConfig`, which makes the application layer depend on an inbound adapter. Either move that config out of inbound or remove the sample greeting feature when skeleton endpoints are cleaned up.

### 5. Revisit skeleton endpoints

`/hello` and `/api/v1/items` are template/sample features and are not part of the Dos Comas planning language in `CONTEXT.md`. Keep them only while they are useful as scaffolding; otherwise remove or isolate them so they do not distract from the planning bounded context.

### 6. Tighten architecture tests

Extend `ArchitectureShould` to guard the boundaries that the current tests miss:

- Application code must not depend on inbound adapters.
- Outbound adapters must not depend on inbound adapters.
- Shared code must not depend on feature code.
- Domain purity should include framework/persistence/serialization packages, with explicit documented exceptions; `AccountDisplay` using `JsonObject` remains the ADR 0003 exception.

### 7. Other behavior-preserving cleanup

Add these to the mechanical cleanup batch before or alongside the invariant refactor:

- Change `AccountRepository.save` to use `INSERT ... RETURNING *`, matching `SpaceRepository.save`, instead of insert plus re-select.
- Harden generic `500` handling: return a generic client message, record the exception on the active span, and revisit the blanket `NumberFormatException -> 400` mapping so server bugs are not accidentally reported as client ID errors.
- Remove the unused `ItemRepository.deleteAll()` production method; table cleanup belongs in test fixtures.
- Remove the unused `NanoIds.size` constructor parameter, or make validation honor it. Prefer removing it as YAGNI.
- If `/api/v1/items` remains as a sample, align its timestamp/domain-construction style with planning; otherwise delete the sample feature with the skeleton endpoint cleanup.

Refactor in small TDD-safe steps and keep the public HTTP behavior and OpenAPI route coverage unchanged unless a route cleanup is intentionally scoped.

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

Account display is separate from core financial account data, but composed into account API responses as a nested JSON object. The backend treats the object's internal fields as client-owned presentation data.

Rules:

- Store display as JSON text in `accounts.display_json`.
- Validate the HTTP shape at the edge: `display` must be a top-level JSON object when present.
- Keep display as an `AccountDisplay` value class around the JSON object; do not model keys such as `initials`, `color`, `typeLabel`, or `subtitle` in the domain or repository.
- `display` may be omitted or partial on create.
- Empty display response is `"display": {}`.
- Display changes update the parent account `updatedAt`.
- No separate display endpoints yet.

## PATCH semantics

Use ordinary `application/json` partial objects, not `application/merge-patch+json`.

Rules:

- Missing fields are unchanged.
- Explicit `null` clears nullable fields, including `note`.
- Explicit `null` for non-nullable fields returns `400`.
- If `display` is omitted, display is unchanged.
- If `display: null`, clear all display to `{}`.
- If `display` is an object, replace the whole display object.
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
  display_json                  TEXT    NOT NULL DEFAULT '{}'
    CHECK (json_valid(display_json) AND json_type(display_json) = 'object' AND length(CAST(display_json AS BLOB)) <= 65536),
  created_at                    INTEGER NOT NULL,
  updated_at                    INTEGER NOT NULL
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
- Apply practical limits:
  - name: 120 characters
  - note: 500 characters
  - display: top-level JSON object up to 64 KiB when serialized
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
- patch updates balance
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
