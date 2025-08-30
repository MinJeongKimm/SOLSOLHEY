-- Add auditing columns to user_challenge_cycle to match BaseEntity fields
ALTER TABLE user_challenge_cycle
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE user_challenge_cycle
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Optional comments
COMMENT ON COLUMN user_challenge_cycle.created_at IS '엔티티 생성 시각';
COMMENT ON COLUMN user_challenge_cycle.updated_at IS '엔티티 수정 시각';

