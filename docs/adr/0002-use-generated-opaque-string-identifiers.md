# Use generated opaque string identifiers

Spaces and accounts use generated opaque NanoID-style string identifiers in the API instead of numeric IDs, composite public keys, or name-derived slugs. IDs are stable, meaningless, immutable, and prefixed by resource type (`sp_...`, `acc_...`); accounts still belong to spaces through `spaceId`, and account routes remain nested under spaces so future auth can authorize by space without inventing current-space semantics.
