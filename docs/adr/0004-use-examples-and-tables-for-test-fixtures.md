# Use Examples and Tables for test fixtures

Test fixtures use immutable `*Example` data classes for valid domain-shaped defaults, `*Table` objects for database side
effects, and `*Row` data classes for database results or insert receipts. Examples use domain types such as `Instant`,
`Money`, `SpaceId`, `AccountId`, and enums; table methods are the translation boundary that bind those values into
storage columns such as epoch milliseconds, cents, raw strings, and API enum values.

This keeps tests readable without making storage details leak into setup data. A typical setup creates an example and
passes it to a table adapter:

```kotlin
val account = AccountExample(
  spaceId = ctx.spaceIds.last(),
  name = "Cash",
  note = "Main account"
)
val row = AccountTable.insert(account)
```

`*Row` types stay minimal when they are only used to pass generated IDs back to scenarios. If a test needs to assert raw
database state, row fields should use storage-shaped names and types, for example `balanceCents: Long` or
`createdAtMillis: Long`.

Also investigated the use of Kotlin's DSL or type-safe builders but... meh. So, avoid helper DSLs such as
`insertAccount { ... }` and avoid duplicating full field lists on table insert signatures unless a test must create
invalid or partial raw database state.

If tests later need real domain objects directly, add an explicit `toDomain()`
conversion or a separate domain fixture at that point rather than adding unused conversion methods now.
