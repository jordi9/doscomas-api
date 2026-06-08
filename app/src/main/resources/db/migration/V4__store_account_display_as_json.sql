ALTER TABLE accounts ADD COLUMN display_json TEXT NOT NULL DEFAULT '{}'
  CHECK (json_valid(display_json) AND json_type(display_json) = 'object' AND length(CAST(display_json AS BLOB)) <= 65536);

UPDATE accounts
SET display_json = (
  SELECT json_patch(
    json_patch(
      json_patch(
        CASE WHEN initials IS NULL THEN '{}' ELSE json_object('initials', initials) END,
        CASE WHEN color IS NULL THEN '{}' ELSE json_object('color', color) END
      ),
      CASE WHEN type_label IS NULL THEN '{}' ELSE json_object('typeLabel', type_label) END
    ),
    CASE WHEN subtitle IS NULL THEN '{}' ELSE json_object('subtitle', subtitle) END
  )
  FROM account_displays
  WHERE account_displays.account_id = accounts.id
)
WHERE EXISTS (
  SELECT 1
  FROM account_displays
  WHERE account_displays.account_id = accounts.id
);

DROP TABLE account_displays;
