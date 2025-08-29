-- 친구 상호작용 테이블 생성
-- 엔티티: com.solsolhey.friend.entity.FriendInteraction

CREATE TABLE IF NOT EXISTS friend_interactions (
    interaction_id   BIGSERIAL PRIMARY KEY,
    from_user_id     BIGINT NOT NULL,
    to_user_id       BIGINT NOT NULL,
    interaction_type VARCHAR(20) NOT NULL,
    message          VARCHAR(200),
    is_read          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 외래키 제약 (users.user_id)
ALTER TABLE friend_interactions
    ADD CONSTRAINT fk_friend_interactions_from_user
        FOREIGN KEY (from_user_id) REFERENCES users(user_id) ON DELETE CASCADE;

ALTER TABLE friend_interactions
    ADD CONSTRAINT fk_friend_interactions_to_user
        FOREIGN KEY (to_user_id) REFERENCES users(user_id) ON DELETE CASCADE;

-- 조회/집계용 인덱스
CREATE INDEX IF NOT EXISTS ix_friend_interactions_to_user ON friend_interactions(to_user_id);
CREATE INDEX IF NOT EXISTS ix_friend_interactions_from_to ON friend_interactions(from_user_id, to_user_id);
CREATE INDEX IF NOT EXISTS ix_friend_interactions_created_at ON friend_interactions(created_at DESC);

