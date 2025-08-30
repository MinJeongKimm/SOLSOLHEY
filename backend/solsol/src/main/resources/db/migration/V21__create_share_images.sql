-- 공유 이미지 테이블 생성 (엔티티: com.solsolhey.share.entity.ShareImage)

CREATE TABLE IF NOT EXISTS share_images (
    image_id          BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL,
    template_id       BIGINT,
    image_url         VARCHAR(500) NOT NULL,
    original_filename VARCHAR(255),
    file_size         BIGINT,
    width             INTEGER,
    height            INTEGER,
    image_type        VARCHAR(50) NOT NULL,
    share_count       INTEGER NOT NULL DEFAULT 0,
    is_public         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_share_images_user ON share_images(user_id);
CREATE INDEX IF NOT EXISTS ix_share_images_public ON share_images(is_public);
CREATE INDEX IF NOT EXISTS ix_share_images_created_at ON share_images(created_at DESC);

ALTER TABLE share_images
    ADD CONSTRAINT fk_share_images_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

ALTER TABLE share_images
    ADD CONSTRAINT fk_share_images_template
        FOREIGN KEY (template_id) REFERENCES share_templates(template_id) ON DELETE SET NULL;

