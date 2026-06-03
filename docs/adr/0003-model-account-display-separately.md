# Model account display separately

Account display details such as initials, color, type label, and subtitle are modeled separately from the core account even though account API responses compose them as a nested `display` object. This keeps presentation preferences out of the financial account model while preserving the current product UI contract; persistence uses a separate `account_displays` table instead of top-level account columns or an arbitrary JSON blob.
