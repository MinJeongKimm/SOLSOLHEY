-- 친구 관계 테이블 생성 (엔티티: com.solsolhey.friend.entity.Friend)

CREATE TABLE IF NOT EXISTS friends (
    friend_id      BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL,
    friend_user_id BIGINT NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 양방향 친구 관계 조회 성능을 위한 인덱스
CREATE INDEX IF NOT EXISTS ix_friends_user ON friends(user_id);
CREATE INDEX IF NOT EXISTS ix_friends_friend_user ON friends(friend_user_id);
CREATE INDEX IF NOT EXISTS ix_friends_status ON friends(status);

-- 외래키 제약 (users.user_id)
ALTER TABLE friends
    ADD CONSTRAINT fk_friends_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

ALTER TABLE friends
    ADD CONSTRAINT fk_friends_friend_user
        FOREIGN KEY (friend_user_id) REFERENCES users(user_id) ON DELETE CASCADE;

