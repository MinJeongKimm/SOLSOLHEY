-- 공유 링크 테이블 생성 (엔티티: com.solsolhey.share.entity.ShareLink)

CREATE TABLE IF NOT EXISTS share_links (
    link_id         BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    link_code       VARCHAR(20) NOT NULL UNIQUE,
    title           VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    thumbnail_url   VARCHAR(500),
    target_url      VARCHAR(1000),
    share_type      VARCHAR(50) NOT NULL,
    click_count     INTEGER NOT NULL DEFAULT 0,
    expires_at      TIMESTAMP,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_share_links_user ON share_links(user_id);
CREATE INDEX IF NOT EXISTS ix_share_links_active ON share_links(is_active);

ALTER TABLE share_links
    ADD CONSTRAINT fk_share_links_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

