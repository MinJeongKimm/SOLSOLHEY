-- 공유 템플릿 테이블 생성 (엔티티: com.solsolhey.share.entity.ShareTemplate)

CREATE TABLE IF NOT EXISTS share_templates (
    template_id      BIGSERIAL PRIMARY KEY,
    template_name    VARCHAR(100) NOT NULL,
    template_content TEXT NOT NULL,
    template_type    VARCHAR(50) NOT NULL,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    preview_url      VARCHAR(500),
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 활성 템플릿 조회 최적화
CREATE INDEX IF NOT EXISTS ix_share_templates_active ON share_templates(is_active);
CREATE INDEX IF NOT EXISTS ix_share_templates_created_at ON share_templates(created_at DESC);

