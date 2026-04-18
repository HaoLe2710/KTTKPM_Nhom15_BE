DROP INDEX IF EXISTS idx_otps_user_email;

ALTER TABLE otps
    DROP COLUMN IF EXISTS user_id;
