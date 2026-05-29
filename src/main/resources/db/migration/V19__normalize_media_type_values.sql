-- Normalize legacy/dev seed media types so JPA enum mapping can deserialize them.
UPDATE media
SET type = UPPER(type)
WHERE type IS NOT NULL
  AND type <> UPPER(type)
  AND UPPER(type) IN ('IMAGE', 'VIDEO');
