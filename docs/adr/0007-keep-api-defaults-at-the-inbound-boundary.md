# Keep API defaults at the inbound boundary

When a default answers "what does this API do when the client omits a field?", keep that default in the inbound layer
and document it in OpenAPI. Kotlin default values are fine on inbound request DTOs because they are part of the JSON
contract translation, and they keep the handler code small.

Application commands and use cases should receive explicit, normalized values. Avoid constructor defaults there unless
the application layer truly owns the creation policy independently of HTTP. This keeps future callers from accidentally
depending on hidden defaults and makes command construction show the complete instruction being executed. If an API uses
`null` or omission to mean "clear/default this non-null domain field", translate that at the inbound boundary too; keep
`null` in commands only when `null` is a real target value.

Domain types may still expose defaults when the default is a real domain value rather than an API omission rule. For
example, `AccountDisplay()` means "no display metadata" and remains useful across inbound, domain logic, persistence
mapping, and tests.

If we ever need to distinguish "client omitted this" from "client explicitly sent the default", model that distinction
in the request/command shape instead of relying on defaulted values.
