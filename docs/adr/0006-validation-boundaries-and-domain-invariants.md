# Validate contracts at adapters and invariants in domain

Dos Comas validates at the boundary that owns the question: inbound handlers reject or translate HTTP/JSON contract
problems, use cases check action context, and domain/value objects guard invariants even when handlers already filtered
input. For user/data constraints we use a small throwing `validate(...)` helper with the fail-fast feel of Kotlin
`require(...)`, while keeping `require(...)`/`check(...)` for programmer preconditions; no field codes, accumulated
errors, or validation framework until clients need them. This follows Cockburn's adapter boundary, Clean Architecture's
inward dependency/business-rule split, DDD's always-valid domain guidance, Fowler's warning that Notification/results
are only worth it when expected input errors need accumulation.

Reference table:

| Source | Useful point for this decision |
| --- | --- |
| Cockburn, Hexagonal Architecture | Adapters translate external technology-specific input into application calls; HTTP/JSON contract checks belong at the adapter boundary. |
| Robert C. Martin, Clean Architecture | Controllers/adapters translate data, while use cases and entities own application/domain rules; dependencies point inward. |
| Microsoft DDD guidance | Domain entities and value objects should enforce invariants and should not be able to exist invalid. |
| Fowler, Contextual Validation | Validation depends on the action/context; avoid one generic context-free `isValid`. |
| Fowler, Notification in Validations | Raw input failures are expected; use Notification/results when we need to report multiple user-correctable errors instead of throwing. |
| Enterprise Craftsmanship, Always-Valid Domain Model | Invalid external input can be filtered before the domain, but the domain still guards invariants as the last line of defense. |
