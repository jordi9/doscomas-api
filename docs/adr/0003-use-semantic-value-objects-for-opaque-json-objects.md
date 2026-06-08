# Use semantic value objects for opaque JSON objects

Some product concepts are JSON objects whose internal schema is owned by the client/UI, while the backend still owns their lifecycle, authorization, persistence, and safety constraints. Account display is the first example: it is presentation data, not part of an account's financial meaning, and the account API composes it as a nested `display` object.

## Decision

Represent opaque JSON-object concepts with named domain value objects, such as `AccountDisplay`, wrapping a JSON object. Do not represent them as raw strings, generic maps, or fake rich domain objects with fields the backend does not own.

For these values, the backend owns only container-level invariants:

- the value is a JSON object, not an array, scalar, or null
- the object may be empty when that is a real domain value
- the serialized object stays within the storage size limit
- the value belongs to and changes with its parent resource
- writes are authorized through the parent use case

The backend does not own or validate internal client keys unless backend behavior starts depending on them.

## Rationale

Strict domain purity would avoid JSON-library types in the domain. We accept this as a pragmatic exception because an opaque `JsonObject` wrapped in a semantic value object is less wrong than the alternatives:

- raw `String` leaks interchange/persistence format and makes invalid states easy
- `Map<String, Any?>` is not really JSON and loses JSON-specific type safety
- typed fields such as `initials`, `color`, `typeLabel`, or `subtitle` pretend the backend owns a schema it does not use
- a named value object still gives the domain a meaningful concept and prevents mixing unrelated JSON documents

## Guardrails

- Each opaque JSON object gets a domain-specific name (`AccountDisplay`, future `UserDisplaySettings`, etc.).
- Do not pass raw JSON strings through domain/application code.
- Do not expose generic maps for these values.
- Keep JSON parsing, serialization, HTTP null/omission semantics, and persistence-column details in adapters.
- Construct empty JSON objects explicitly at the boundary or factory that owns that policy; do not hide API defaults in domain constructor defaults.
- If the backend starts reading, constraining, querying, or making decisions from a JSON key, that key is no longer opaque and must become an explicit domain concept.

## Current application

`AccountDisplay` wraps the JSON object received for account `display`. The HTTP adapter validates that `display` is an object when present, maps omitted or `null` display according to the endpoint semantics, and enforces the serialized size limit. Persistence stores it on `accounts.display_json` as JSON text with database checks for valid object JSON and size. We do not keep a separate `account_displays` table or per-display-field columns unless backend behavior later depends on specific display fields.
