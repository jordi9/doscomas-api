# DDD Context and Package Boundaries

Use this before creating or renaming top-level `feature/[name]` packages, moving code between features, or deciding whether something is a bounded context, subpackage, entity, resource, or shared concept.

## Core Rule

Top-level feature packages represent business capabilities / bounded contexts, not database tables, HTTP resources, UI screens, or nouns by default.

```text
Good top-level feature names:
feature/ordering/
feature/billing/
feature/identity/

Suspicious top-level feature names when based only on resources:
feature/order/
feature/customer/
feature/status/
```

A resource can be an API subpackage without becoming a bounded context:

```text
feature/ordering/
├── domain/
│   ├── Order.kt
│   └── Shipment.kt
├── inbound/
│   ├── order/
│   └── shipment/
└── outbound/
    ├── order/
    └── shipment/
```

## Bounded Context vs Entity/Resource

A bounded context is a language and ownership boundary. An entity/resource is a concept inside that language.

Ask what owns the meaning of the words, not just what appears in the URL or database.

| Question | Same context signal | Separate context signal |
| --- | --- | --- |
| Meaning | Same word means the same thing | Same word means different things |
| Rules | Rules change together | Rules evolve independently |
| Ownership | Same product/business owner | Different owner or source of truth |
| Lifecycle | Objects are created/changed together | Independent lifecycle |
| Coupling | Changes touch nearby concepts | Public contract/event is enough |

Example pattern:

```text
Ordering = likely bounded context / feature
Order = concept inside Ordering
Shipment = concept inside Ordering
OrderDisplay = HTTP/display concern unless domain rules emerge
```

Do not promote `Order` or `Shipment` to top-level features just because they have handlers, repositories, or tables.

## Package Shape Inside a Feature

Prefer capability first, layer second, sub-concept third:

```text
feature/ordering/
├── domain/                 # Ordering domain language
├── application/            # Ordering use cases
├── inbound/
│   ├── order/              # Order HTTP API inside Ordering
│   └── shipment/           # Shipment HTTP API inside Ordering
└── outbound/
    ├── order/              # Order persistence adapter inside Ordering
    └── shipment/           # Shipment persistence adapter inside Ordering
```

Avoid technical grouping that separates files that change together:

```text
# Avoid
feature/ordering/inbound/handlers/
feature/ordering/inbound/mappers/
feature/ordering/inbound/responses/
feature/ordering/inbound/helpers/
```

In adapters, subpackages by API/persistence concept are OK:

```text
# Good
feature/ordering/inbound/order/PatchOrderHandler.kt
feature/ordering/inbound/order/OrderResponse.kt
feature/ordering/inbound/order/OrderStatusHttp.kt
feature/ordering/outbound/order/OrderRepository.kt
feature/ordering/outbound/order/OrderRowMapper.kt
```

## When to Split a New Feature/Context

Create a new top-level feature only when there is evidence of a separate business capability or bounded context.

Good reasons to split:

- The same term has a different meaning in another area (`CustomerAccount` vs `LedgerAccount`).
- Another system/module is the source of truth.
- Business rules and release cadence evolve independently.
- The feature can expose a stable public contract to other features.
- The model would remain coherent if extracted as a service/library later.

Weak reasons to split:

- There is a table for it.
- There is a REST resource for it.
- The file count is growing.
- The UI has a screen for it.
- It has its own repository class.

Start inside the existing context when unsure. Extract later when language/ownership diverges.

## Shared Code Rules

`shared/` is for generic technical or cross-cutting code, not a dumping ground for business concepts.

Good shared candidates:

```text
shared/domain/Validation.kt
shared/domain/NanoIds.kt
shared/inbound/handler/ErrorHandling.kt
shared/outbound/db/DatabaseMigrations.kt
shared/outbound/metrics/MeterRegistryProvider.kt
```

Suspicious shared candidates:

```text
shared/domain/Order.kt
shared/domain/User.kt
shared/domain/Category.kt
shared/domain/Status.kt
```

Some value objects can be shared if they are genuinely universal and do not carry context-specific rules. If the meaning differs by context, keep separate types even if fields look identical.

```text
feature/ordering/domain/Money.kt       # OK if ordering-specific behavior/format exists
shared/domain/Money.kt                 # Only if truly universal across contexts
```

## Cross-Feature Communication

Avoid direct domain-to-domain imports across features.

Prefer:

- Application use cases as the public entry point.
- IDs/value references instead of importing another feature's aggregate.
- Events or explicit contracts when workflows cross context boundaries.
- Duplication of small context-specific models when meanings differ.

```text
# Avoid
feature/billing/domain imports feature/ordering/domain/Order

# Prefer
feature/billing/application calls an ordering application API, consumes an event, or stores OrderId
```

## Naming Heuristics

Use ubiquitous language and business capability names.

Prefer names that answer "what business capability is this?":

```text
ordering
billing
identity
notifications
```

Be careful with generic nouns:

```text
account
user
status
category
profile
```

If a generic noun is necessary, qualify it with context language:

```text
CustomerAccount
LedgerAccount
SalesOrder
PurchaseOrder
```

## Decision Checklist

Before creating a new `feature/[name]`:

- [ ] Is this a business capability, not just a resource/table?
- [ ] Does it have its own language and rules?
- [ ] Does it have separate ownership or source of truth?
- [ ] Can other features interact with it through a public contract?
- [ ] Would putting it inside an existing context create language confusion?

If most answers are "no", keep it as a sub-concept inside the existing feature.
