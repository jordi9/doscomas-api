# NOW

The first Spaces/Accounts API slice from `PLAN.md` is implemented and still under review. Current focus remains validation boundaries: HTTP/JSON contract checks in inbound, action-context checks in use cases/domain services, and domain invariants in domain code.

`docs/adr/0006-validation-boundaries-and-domain-invariants.md` captures the validation decision. The local code pass keeps create handlers as Ktor/kotlinx DTOs for required-field conversion, unwraps missing-field conversion errors into messages like `balance is required`, uses `JsonPrimitive` only where default conversion was too permissive (money must be a JSON string), keeps PATCH manual because of partial/null semantics, and maps planning validation errors to `400`.

Validation run for the local pass: `./gradlew spotlessApply`, `./gradlew test`, and `./gradlew build` all passed.
