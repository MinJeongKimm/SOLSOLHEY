-- Add unique index to ensure one mascot per user
CREATE UNIQUE INDEX IF NOT EXISTS ux_mascots_user_id ON mascots(user_id);

-- Enforce idempotency on votes by unique idempotency_key when provided
CREATE UNIQUE INDEX IF NOT EXISTS ux_votes_idempotency_key ON votes(idempotency_key);

