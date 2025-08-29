-- 마스코트 테이블 생성 (엔티티: com.solsolhey.mascot.domain.Mascot)

CREATE TABLE IF NOT EXISTS mascots (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    name                VARCHAR(50) NOT NULL,
    type                VARCHAR(20) NOT NULL,
    equipped_item       VARCHAR(100),
    equipped_layout     TEXT,
    snapshot_image      TEXT,
    background_id       VARCHAR(50) NOT NULL DEFAULT 'bg_room_basic',
    background_color    VARCHAR(7),
    background_pattern  VARCHAR(20),
    exp                 INTEGER NOT NULL DEFAULT 0,
    level               INTEGER NOT NULL DEFAULT 1,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 유저별 1마스코트 보장 (애플리케이션 로직과 일치)
CREATE UNIQUE INDEX IF NOT EXISTS ux_mascots_user_id ON mascots(user_id);

-- FK: users(user_id)
ALTER TABLE mascots
    ADD CONSTRAINT fk_mascots_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

