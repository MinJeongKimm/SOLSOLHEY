-- 사용자-챌린지 참여 테이블 생성 (엔티티: com.solsolhey.challenge.entity.UserChallenge)

CREATE TABLE IF NOT EXISTS user_challenge (
    user_challenge_id BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL,
    challenge_id      BIGINT NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    progress_count    INTEGER NOT NULL DEFAULT 0,
    target_count      INTEGER NOT NULL,
    started_at        TIMESTAMP,
    completed_at      TIMESTAMP,
    progress_data     TEXT,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX IF NOT EXISTS ix_user_challenge_user ON user_challenge(user_id);
CREATE INDEX IF NOT EXISTS ix_user_challenge_challenge ON user_challenge(challenge_id);
CREATE INDEX IF NOT EXISTS ix_user_challenge_status ON user_challenge(status);

-- 외래키 제약
ALTER TABLE user_challenge
    ADD CONSTRAINT fk_user_challenge_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

ALTER TABLE user_challenge
    ADD CONSTRAINT fk_user_challenge_challenge
        FOREIGN KEY (challenge_id) REFERENCES challenge(challenge_id) ON DELETE CASCADE;

