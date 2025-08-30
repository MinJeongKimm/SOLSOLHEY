-- Add content hash to mascot_snapshots to deduplicate identical images
ALTER TABLE mascot_snapshots
    ADD COLUMN IF NOT EXISTS content_hash VARCHAR(64);

-- Helpful index for finding duplicates per user
CREATE INDEX IF NOT EXISTS ix_mascot_snapshots_user_hash
    ON mascot_snapshots(user_id, content_hash);

