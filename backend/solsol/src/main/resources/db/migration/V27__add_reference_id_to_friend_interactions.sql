-- Add reference_id column to friend_interactions for linking to source entities (e.g., friends.friend_id)
ALTER TABLE friend_interactions
    ADD COLUMN IF NOT EXISTS reference_id BIGINT;

-- Optional: index for faster lookups by reference
CREATE INDEX IF NOT EXISTS idx_friend_interactions_reference_id
    ON friend_interactions (reference_id);

