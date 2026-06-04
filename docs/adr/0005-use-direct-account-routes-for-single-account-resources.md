# Use direct account routes for single-account resources

Supersedes [0002](0002-use-generated-opaque-string-identifiers.md). Spaces and accounts still use generated opaque public IDs (`sp_...`, `acc_...`), and accounts still belong to spaces through `spaceId`. Because account IDs are globally unique, single-account reads and updates should address the account directly instead of requiring both the parent space ID and the account ID.

Keep space-scoped collection routes where the space is the actual selection context:

```http
GET  /api/v1/spaces/{spaceId}/accounts
POST /api/v1/spaces/{spaceId}/accounts
```

Use direct account routes for individual account resources:

```http
GET   /api/v1/accounts/{accountId}
PATCH /api/v1/accounts/{accountId}
```

Account responses continue to include `spaceId`. Future authorization should resolve the account's space from the account ID and check the caller's access to that space, rather than duplicating the parent ID in the URL. Do not introduce top-level account list/create routes yet; without auth or current-space semantics, list/create remain space-scoped.
