ALTER TABLE options
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE option_values
    ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0;

CREATE UNIQUE INDEX IF NOT EXISTS idx_options_code_lower_unique ON options ((LOWER(code)));
CREATE UNIQUE INDEX IF NOT EXISTS idx_option_values_option_value_lower_unique ON option_values (option_id, LOWER(value));

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'search_synonym_groups_code_key'
    ) THEN
        ALTER TABLE search_synonym_groups DROP CONSTRAINT search_synonym_groups_code_key;
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS idx_search_synonym_groups_locale_code_unique
    ON search_synonym_groups (locale, code);

CREATE UNIQUE INDEX IF NOT EXISTS idx_search_suggestions_locale_keyword_unique
    ON search_suggestions (locale, keyword_normalized);

ALTER TABLE search_projection_failures
    ADD COLUMN IF NOT EXISTS state VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    ADD COLUMN IF NOT EXISTS resolution_type VARCHAR(30),
    ADD COLUMN IF NOT EXISTS resolution_note VARCHAR(500),
    ADD COLUMN IF NOT EXISTS last_retried_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE search_projection_failures
SET state = CASE
        WHEN resolved_at IS NOT NULL THEN 'RESOLVED_AUTOMATIC'
        ELSE 'OPEN'
    END,
    resolution_type = CASE
        WHEN resolved_at IS NOT NULL THEN 'AUTOMATIC'
        ELSE resolution_type
    END,
    updated_at = CURRENT_TIMESTAMP
WHERE state IS NULL
   OR resolution_type IS NULL;

CREATE INDEX IF NOT EXISTS idx_search_projection_failures_state_failed_at
    ON search_projection_failures (state, failed_at DESC);

CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id VARCHAR(255) PRIMARY KEY,
    action_type VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(255),
    actor_role VARCHAR(50) NOT NULL,
    actor_id VARCHAR(255),
    summary_payload TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_admin_audit_logs_created_at
    ON admin_audit_logs (created_at DESC);
