# Use generated opaque string identifiers

Status: Superseded by [0005](0005-use-direct-account-routes-for-single-account-resources.md).

Spaces and accounts use generated opaque NanoID-style string identifiers in the API instead of numeric IDs, composite public keys, or name-derived slugs. IDs are stable, meaningless, immutable, and prefixed by resource type (`sp_...`, `acc_...`); accounts still belong to spaces through `spaceId`, and account routes remain nested under spaces so future auth can authorize by space without inventing current-space semantics.

Historical note: ADR 0005 keeps generated opaque IDs but replaces the nested single-account route decision with direct account routes.
