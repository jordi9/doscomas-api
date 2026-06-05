# NOW

The first Spaces/Accounts API slice from `PLAN.md` is implemented.

## Focus

The current focus remains in review on the validation-boundaries surface.
`docs/adr/0006-validation-boundaries-and-domain-invariants.md` is the durable decision: inbound owns HTTP/JSON contract
checks, use cases/domain services own action-context checks, and domain code guards invariants.

The useful validation-error pass is committed (`e9470ef`). It keeps create handlers on Ktor/kotlinx DTO conversion, uses
`JsonPrimitive` only for strict money-string contract checks, keeps PATCH manual for partial/null semantics, and maps
planning validation failures to `400`.

Stay on this surface if continuing: inspect
`app/src/main/kotlin/com/jordi9/doscomas/shared/inbound/handler/ErrorHandling.kt` first and keep it boring. The
remaining review concern is whether the missing-field unwrap and request-error helpers are still small enough; do not
add `RequestValidation` or broader error machinery unless the current shape actually grows. No validation gap is open;
the relevant pass ran `./gradlew spotlessApply`, `./gradlew test`, and `./gradlew build` successfully.
