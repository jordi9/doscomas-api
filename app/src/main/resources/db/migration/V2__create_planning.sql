CREATE TABLE spaces (
  id          TEXT    PRIMARY KEY,
  name        TEXT    NOT NULL,
  created_at  INTEGER NOT NULL,
  updated_at  INTEGER NOT NULL
);

CREATE TABLE accounts (
  id                            TEXT    PRIMARY KEY,
  space_id                      TEXT    NOT NULL REFERENCES spaces(id),
  name                          TEXT    NOT NULL,
  category                      TEXT    NOT NULL,
  balance_cents                 INTEGER NOT NULL,
  monthly_contribution_cents    INTEGER NOT NULL,
  currency                      TEXT    NOT NULL,
  note                          TEXT,
  balance_updated_at            INTEGER NOT NULL,
  created_at                    INTEGER NOT NULL,
  updated_at                    INTEGER NOT NULL
);

CREATE TABLE account_displays (
  account_id  TEXT PRIMARY KEY REFERENCES accounts(id),
  initials    TEXT,
  color       TEXT,
  type_label  TEXT,
  subtitle    TEXT,
  CHECK (initials IS NOT NULL OR color IS NOT NULL OR type_label IS NOT NULL OR subtitle IS NOT NULL)
);
