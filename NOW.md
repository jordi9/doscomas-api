# NOW

The first Spaces/Accounts API slice from `PLAN.md` is implemented.

## Focus

The current focus is still the validation-boundaries/PATCH-shape review. `docs/adr/0006-validation-boundaries-and-domain-invariants.md` remains the durable decision: inbound owns HTTP/JSON contract checks, use cases/domain services own action-context checks, and domain code guards invariants.

Recent cleanup simplified account PATCH: domain mutation is now `FieldUpdate.Keep/Replace`, `AccountChanges` requires explicit updates for every editable field, `display` is a fixed-shape JSON metadata object replaced as a whole, shared inbound `AccountDisplayRequest` handles display decoding, and reusable JSON-to-update helpers live in `app/src/main/kotlin/com/jordi9/doscomas/feature/planning/inbound/JsonFieldUpdate.kt`. `SERVER_OWNED_FIELDS`, `DisplayChange`, `NullableField`/`PatchField`, and `optionalString` are gone.

Stay on this same surface if continuing: review `PatchAccountHandler.kt`, `JsonFieldUpdate.kt`, `Account.kt`, and OpenAPI/test wording for whether the helper names and `FieldUpdate` boundary still feel boring enough. No validation gap is open; the latest pass ran `./gradlew spotlessApply`, `./gradlew test`, and `./gradlew build` successfully.
